# FirebaseAuthProviderLinking
Shows how the auth provider can be linked to a firebase user. 

## Current Status: 

demo for showing the issues related to linking mutiple auth providers and not able to retrevie all the attached email address.

## How to run

- create a  firebase project with facebook, google and email password authentication enabled.
- create a facebook application to link with the app, and putting secret on firebase console.
- put google-services.json file in `app/`
- setup facebook login as per the given instructions

## Issues:
- Email address of all the providers connected cannot be retrieved from `firebaseUser.gerProviderData()` list. Id for each provider available but not email.
- After signup with facebook login, linking email password login, then if email provider is unlinked, facebook email is not available 
any where in the user object.
- After facebook login, linking Google provider set the default email to the Google provider's
then if Google provider is unlinked, the email of the Google provider is not replaced with facebook. Google is now not in the providers list
- After Google login, linking Facebook provider **does not** set the default email to the Facebook provider's
  then if Google provider is unlinked, the email of the Google provider is not replaced with facebook. Google is now not in the providers list



