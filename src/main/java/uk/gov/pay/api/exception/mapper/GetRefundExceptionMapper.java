package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.GetRefundException;
import uk.gov.pay.api.model.RequestError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.RequestError.Code.GET_PAYMENT_REFUND_CONNECTOR_ERROR;
import static uk.gov.pay.api.model.RequestError.Code.GET_PAYMENT_REFUND_NOT_FOUND_ERROR;
import static uk.gov.pay.api.model.RequestError.aRequestError;

public class GetRefundExceptionMapper implements ExceptionMapper<GetRefundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetRefundExceptionMapper.class);

    @Override
    public Response toResponse(GetRefundException exception) {

        RequestError requestError;
        Response.Status status;

        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            requestError = aRequestError(GET_PAYMENT_REFUND_NOT_FOUND_ERROR);
            status = NOT_FOUND;
        } else {
            requestError = aRequestError(GET_PAYMENT_REFUND_CONNECTOR_ERROR);
            status = INTERNAL_SERVER_ERROR;
            LOGGER.error("Connector invalid response was {}.\n Returning http status {} with error body {}", exception.getMessage(), status, requestError);
        }

        return Response
                .status(status)
                .entity(requestError)
                .build();
    }
}
