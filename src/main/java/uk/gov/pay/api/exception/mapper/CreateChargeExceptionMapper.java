package uk.gov.pay.api.exception.mapper;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.RequestError;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.RequestError.Code.ACCOUNT_NOT_LINKED_WITH_PSP;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_ACCOUNT_ERROR;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_AGREEMENT_ID_ERROR;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_AUTHORISATION_API_NOT_ENABLED;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_CONNECTOR_ERROR;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_MOTO_NOT_ENABLED;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.RequestError.Code.RESOURCE_ACCESS_FORBIDDEN;
import static uk.gov.pay.api.model.RequestError.aRequestError;

public class CreateChargeExceptionMapper implements ExceptionMapper<CreateChargeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChargeExceptionMapper.class);

    @Override
    public Response toResponse(CreateChargeException exception) {

        RequestError requestError;
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR_500;

        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            if (exception.getErrorIdentifier() == ErrorIdentifier.AGREEMENT_NOT_FOUND) {
                statusCode = HttpStatus.BAD_REQUEST_400;
                requestError = aRequestError("set_up_agreement", CREATE_PAYMENT_AGREEMENT_ID_ERROR);
            } else {
                requestError = aRequestError(CREATE_PAYMENT_ACCOUNT_ERROR);
            }
        } else {
            ErrorIdentifier errorIdentifier = exception.getErrorIdentifier();
            switch (errorIdentifier) {
                case ZERO_AMOUNT_NOT_ALLOWED:
                    statusCode = HttpStatus.UNPROCESSABLE_ENTITY_422;
                    requestError = aRequestError("amount", CREATE_PAYMENT_VALIDATION_ERROR,
                            "Must be greater than or equal to 1");
                    break;
                case MOTO_NOT_ALLOWED:
                    statusCode = HttpStatus.UNPROCESSABLE_ENTITY_422;
                    requestError = aRequestError(CREATE_PAYMENT_MOTO_NOT_ENABLED);
                    break;
                case TELEPHONE_PAYMENT_NOTIFICATIONS_NOT_ALLOWED:
                    statusCode = HttpStatus.FORBIDDEN_403;
                    requestError = aRequestError(RESOURCE_ACCESS_FORBIDDEN);
                    break;
                case ACCOUNT_NOT_LINKED_WITH_PSP:
                    statusCode = HttpStatus.FORBIDDEN_403;
                    requestError = aRequestError(ACCOUNT_NOT_LINKED_WITH_PSP);
                    break;
                case AUTHORISATION_API_NOT_ALLOWED:
                    statusCode = HttpStatus.UNPROCESSABLE_ENTITY_422;
                    requestError = aRequestError(CREATE_PAYMENT_AUTHORISATION_API_NOT_ENABLED);
                    break;
                default:
                    requestError = aRequestError(CREATE_PAYMENT_CONNECTOR_ERROR);
                    LOGGER.info("Connector invalid response was {}.\n Returning http status {} with error body {}",
                            exception.getMessage(), INTERNAL_SERVER_ERROR, requestError);

            }
        }

        return Response.status(statusCode).entity(requestError).build();
    }
}
