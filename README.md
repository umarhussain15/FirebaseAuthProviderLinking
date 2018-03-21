# FirebaseAuthProviderLinking
Shows how the auth provider can be linked to a firebase user. 

## Current Status: 

Issue Resolved: Email addresses now for all the available provides from `firebaseUser.getProviderData()`. (starting Firebase SDK version 12.0.0)

## How to run

- create a  firebase project with facebook, google and email password authentication enabled.
- create a facebook application to link with the app, and putting secret on firebase console.
- put google-services.json file in `app/`
- setup facebook login as per the given instructions

## What Happens

- **Linking Email Auth**
    - Attaching email provider to an account will also set the email as the default email.
    - Removing the email will set the default email null.
- **Linking Facebook**
    -  If no other auth provider attached i.e. user signup with facebook, facebook email is set default
    -  If Facebook is added as secondary provider, it will not update default email.
- **Linking Google**
    - Behaves same as Facebook Linking


## Issues:
- Email address of all the providers connected cannot be retrieved from `firebaseUser.gerProviderData()` list. Id for each provider available but not email.
    - **Solution**: Fixed in Firebase SDK 12.0.0
- After signup with facebook login, linking email password login, then if email provider is unlinked, facebook email is not available 
any where in the user object.
    - **Solution**: Starting Firebase SDK 12.0.0, emails of all the attached providers are available in `firebaseUser.getProviderData()`. Default email has to be set manually after any auth provider unlink operation.
- After facebook login, linking Google provider set the default email to the Google provider's
then if Google provider is unlinked, the email of the Google provider is not replaced with facebook. Google is now not in the providers list
    - **Solution**: After un-link operation on firebase user, setting default email has to be done manually, accroding to the firebase.
- After Google login, linking Facebook provider **does not** set the default email to the Facebook provider's
  then if Google provider is unlinked, the email of the Google provider is not replaced with facebook. Google is now not in the providers list
  - **Solution**: After un-link operation on firebase user, setting default email has to be done manually, accroding to the firebase.




