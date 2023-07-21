# Setting up configuration file

## -Configuration file overview

Configuration file  contains different options of customizable business flows and essetial settings to run this integration. 

> **/functions/src/main/resources/config.yaml**

**Sample Config File**

```
EXECUTION_MODE: "ACTIVE"
DEFAULT_LOCALE: "en"
CREDENTIALS:
  SIGNIFYD:
    TEAM_API_KEY: "*****"
  COMMERCETOOLS:
    CLIENT_ID: "*****"
    CLIENT_SECRET: "*****"
    PROJECT_KEY: "****"
    REGION: "GCP_EUROPE_WEST1"
DATA_MAPPING:
  PHONE_NUMBER_FIELD:
    customerPhoneNumberField: "phoneNumber"
  PAYMENT_METHODS:
    card: "CREDIT_CARD"
    paypal: "PAYPAL_ACCOUNT"
    bank_transaction: "ACH"
EXCLUDED_PAYMENT_METHODS: [bank_transaction]
DEFAULT_CONFIGURATION:
  DECISION_ACTIONS:
    ACCEPT:
      ACTION_TYPE: "CUSTOM_STATE_TRANSITION"
      CUSTOM_STATE_KEY: "order-workflow-state-confirmed"
      FORCE_TRANSITION: true
    HOLD:
      ACTION_TYPE: "NONE"
    REJECT:
      ACTION_TYPE: "CUSTOM_STATE_TRANSITION"
      CUSTOM_STATE_KEY: "order-workflow-state-rejected"
      FORCE_TRANSITION: true
COUNTRY_CONFIGURATIONS:
  DE:
    PRE_AUTH: true
    SCA_EVALUATION_REQUIRED: false
    RECOMMENDATION_ONLY: false
    DECISION_ACTIONS:
      ACCEPT:
        ACTION_TYPE: "DEFAULT_STATE_TRANSITION"
        DEFAULT_STATE: "Confirmed"
      HOLD:
        ACTION_TYPE: "CUSTOM_STATE_TRANSITION"
        CUSTOM_STATE_KEY: "order-workflow-state-hold"
        FORCE_TRANSITION: true
      REJECT:
        ACTION_TYPE: "DEFAULT_STATE_TRANSITION"
        DEFAULT_STATE: "Cancelled"

```

## -Execution Modes

This configuration facilitates changing general behaviour of integration. Generally can be used on different steps before enabling integration entirely.

| Key      | Behaviour                                                                                                                                                                                                                                                                                                                                                                             |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ACTIVE   | All functionalities of the integration are enabled                                                                                                                                                                                                                                                                                                                                    |
| PASSIVE  | Signifyd connection still intact but fraud decisions are not processed only written on to order itself                                                                                                                                                                                                                                                                                |
| DISABLED | Signifyd Connection disabled, integration still connected to respective componencts of infrastructe but all received request answered 200 with early exit without processing. This feature exist incase of having a major problem in the integration or other affected systems that prevents order creation/processing so without breaking infrastructure integration can be disabled |

## -Credentials

To established connections to commercetools and Signifyd this section needs to be filled.

| Key                         | Comment                                      |
| --------------------------- | -------------------------------------------- |
| SIGNIFYD.TEAM_API_KEY       | Given team API key from Signifyd             |
| COMMERCETOOLS.CLIENT_ID     | Client id of commercetools Api Client        |
| COMMERCETOOLS.CLIENT_SECRET | Client Secret of commercetools Api Client    |
| COMMERCETOOLS.PROJECT_KEY   | Project key of commercetools project         |
| COMMERCETOOLS.REGION        | Region of commercetools project in uppercase |

| ServiceRegion values       | Region                   |
| -------------------------- | ------------------------ |
| `GCP_EUROPE_WEST1`         | europe-west1.gcp         |
| `GCP_US_CENTRAL1`          | us-central1.gcp          |
| `AWS_US_EAST_2`            | us-east-2.aws            |
| `AWS_EU_CENTRAL_1`         | eu-central-1.aws         |
| `GCP_AUSTRALIA_SOUTHEAST1` | australia-southeast1.gcp |

## -Data Mappings

##### Payment Methods

This configuration establishs a mapping for payment method types of commercetools project with Signifyd expected types

Integration sends respective data of commercetools **method** field of [PaymentMethodInfo](https://docs.commercetools.com/api/projects/payments#paymentmethodinfo) in Payment object

Configuration must be set like {{payment-method}} = "{{respective-signifyd-payment-method}}"

Signifyd Payment Method Names

- `CREDIT_CARD`
- `GIFT_CARD`
- `DEBIT_CARD`
- `PREPAID_CARD`
- `SNAP_CARD`
- `ACH`
- `PAYPAL_ACCOUNT`
- `ALI_PAY`
- `APPLE_PAY`
- `AMAZON_PAYMENTS`
- `ANDROID_PAY`
- `BITCOIN`
- `CASH`
- `FREE`
- `GOOGLE_PAY`
- `LOAN`
- `PAYPAL_ACCOUNT`
- `REWARD_POINTS`
- `STORE_CREDIT`
- `SAMSUNG_PAY`
- `VISA_CHECKOUT`
- `VOUCHER`

##### Mapping the Commercetools Order Number with the Signifyd Order Id
    The plugin maps the <b>Order Number</b> of the Commercetools Order with the Order Id of the Signifyd Order during the processing PreAuth/PostAuth flows. If the Commercetools Order does not have an Order Number, the Plugin maps the Order Id of the Commercetools Order as a fallback.

## -Behavioural Configs

##### PreAuth or PostAuth selection

As a default integration configured to work with Post Auth flow enabled to change that to Pre Auth flow PRE_AUTH field must be set as **true**

##### Enabling Sca Evaluation

To enable standardized control assessment prosedures for Pre Auth flow SCA_EVALUATION_REQUIRED field must be set as true, this config only used when Pre Auth flow enabled

##### Decision Actions

Integration provides an different behaviours when Signifyd determines a fraud decision on an order. With given settings this integrations can be easily placed to order workflows.

| Action Type Key          | Behaviour                                                                                                                                                                                        |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| NONE                     | Only updates custom fields of orders                                                                                                                                                             |
| DEFAULT_STATE_TRANSITION | Transition to given default state of order. **DEFAULT_STATE** field must be set with [usable keys](https://docs.commercetools.com/api/projects/orders#orderstate)                                |
| CUSTOM_STATE_TRANSITION  | Transition to given custom state for orders. **CUSTOM_STATE_KEY** needs to be set with custom state key for orders. If forced transition required **FORCE_TRANSITION** field can be set as true. |
| DO_NOT_CREATE_ORDER      | Only usable for Rejection decisions of Pre Auth flow, prevents order creation.                                                                                                                   |

##### Country based configuration logic

To support different configurations for countries our configuration use 

COUNTRY_CONFIGURATIONS part with the country code ([ISO 3166-1 alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)) 

Notdefined countries use DEFAULT_CONFIGURATION.

To determine which country order belong to country field of [Order](https://docs.commercetools.com/api/projects/orders#order) entity is used and this filed needs to be already populated when order is placed.

```
COUNTRY_CONFIGURATIONS:
  DE:
.
.
.
```

## -Other configurations

//todo
