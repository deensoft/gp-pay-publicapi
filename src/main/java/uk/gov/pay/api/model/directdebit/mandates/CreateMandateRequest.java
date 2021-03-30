package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.pay.api.validation.ValidReturnUrl;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "The Payload to create a new Mandate")
public class CreateMandateRequest {

    @NotNull
    @ValidReturnUrl
    @Size(max = 2000, message = "Must be less than or equal to {max} characters length")
    @JsonProperty(value = "return_url")
    private String returnUrl;

    @NotNull
    @NotBlank
    @Size(max = 255, message = "Must be less than or equal to {max} characters length")
    @JsonProperty(value = "reference")
    private String reference;
    
    @JsonProperty(value = "description")
    @Size(min = 1, max = 255, message = "Must have a size between {min} and {max}")
    private String description;

    @Schema(description = "mandate description")
    public String getDescription() {
        return description;
    }

    @Schema(description = "mandate return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    public String getReturnUrl() {
        return returnUrl;
    }

    @Schema(description = "mandate reference", example = "test_service_reference")
    public String getReference() {
        return reference;
    }
}
