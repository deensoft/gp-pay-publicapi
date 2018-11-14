package uk.gov.pay.api.model.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static javax.ws.rs.HttpMethod.GET;

@ApiModel(value = "RefundLinksForSearch", description = "links for search refunds resource")
public class RefundLinksForSearch {

    private static final String SELF = "self";
    private static final String PAYMENT = "payment_url";

    @JsonProperty(value = SELF)
    private Link self;

    @JsonProperty(value = PAYMENT)
    private Link payment;

    @ApiModelProperty(value = SELF, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getSelf() {
        return self;
    }
    
    @ApiModelProperty(value = PAYMENT, dataType = "uk.gov.pay.api.model.links.Link")
    public Link getRefunds() {
        return payment;
    }

    public void addSelf(String href) {
        this.self = new Link(href, GET);
    }

    public void addPayment(String href) {
        this.payment = new Link(href, GET);
    }
}