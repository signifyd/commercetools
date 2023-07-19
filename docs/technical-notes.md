# Technical Notes

## -Mapping the Commercetools Order Number with the Signifyd Order Id
    The plugin maps the <b>Order Number</b> of the Commercetools Order with the Order Id of the Signifyd Order during the processing PreAuth/PostAuth flows. If the Commercetools Order does not have an Order Number, the Plugin maps the Order Id of the Commercetools Order as a fallback.

//todo