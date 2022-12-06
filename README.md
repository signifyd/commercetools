# Signifyd integration for commercetools

---

1. Overview

2. Functional Overview & Integration Guide
   
   1. Use cases
      
      1. [Pre Auth](./docs/preauth.md)
      
      2. [Post Auth](./docs/postauth.md)
      
      3. [Post Sale](./docs/postsale.md)
   
   2. Setting up configuration file
      
      1. [Configuration file overview](./docs/setting-up-configs.md#configuration-file-overview)
      
      2. [Execution Modes](./docs/setting-up-configs.md#execution-modes)
      
      3. [Credentials](./docs/setting-up-configs.md#credentials)
      
      4. [Data Mappings](./docs/setting-up-configs.md#data-mappings)
      
      5. [Behavioural Configs](./docs/setting-up-configs.md#behavioural-configs)
      
      6. Other configurations

3. [Infrastructure](./docs/infrastructure.md#infrastructure)
   
   1. [Serverless Solution](./docs/infrastructure.md#serverless-infrastructure)
      
      1. [Amazon Web Services](./docs/infrastructure.md#amazon-web-services)
      
      2. [Google Cloud Platform](./docs/infrastructure.md#google-cloud-platform)
      
      3. [Azure](./docs/infrastructure.md#azure)
   
   2. [Dockerized Container Soution](./docs/infrastructure.md#dockerized-container-soution)

4. [commercetools and storefront setup](./docs/commercetools-setup.md#-api-client)
   
   1. [API client](./docs/commercetools-setup.md#-api-client)
   
   2. [API extension](./docs/commercetools-setup.md#-api-extension)
   
   3. [Subscription](./docs/commercetools-setup.md#-api-extension)
   
   4. [Custom Fields](./docs/commercetools-setup.md#-api-extension)
   
   5. [Storefront JS script ](./docs/commercetools-setup.md#-api-extension)

5. Signifyd setup

6. [Building and Deployment](./docs/building-deployment.md)
   
   1. Serverless solution
   
   2. [Dockerized Container solution](./docs/building-deployment.md#dockerized-container-solution)

---

## Overview

**Signifyd** is a fraud solution that provides a financial guarantee, allowing businesses to increase sales
while reducing fraud losses. The Signifyd integration for commercetools provides these functionality using five primary API integration points:

1. Pre-payment authorization (Checkout API, Transaction API)
2. Post-payment authorization (Sale API, Webhooks)
3. Post order fulfilment (Fulfilment API)
4. Post order reroute (Reroute API)
5. Post order reprice (Reprice API)

Sale API is used if merchants are executing a "Post-Auth" Flow where they call into Signifyd after the
order has been placed and the payment has been authorized with the Payment Gateway. Checkout API
is used if merchants are executing a "Pre-Auth" Flow where they call into Signifyd before authorizing a
payment with the Payment Gateway. In "Pre-Auth" flow, for each order, there are at least 2 API calls, a
Checkout API call prior to payment authorization and a Transaction API after authorization
