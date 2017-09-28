# FirebaseAuthProviderLinking
Shows how the auth provider can be linked to a firebase user. 

## Current Status: 

demo for showing the issues related to linking mutiple auth providers and not able to retrevie all the attached email address.

## Issues:
- Email address of all the providers connected cannot be retrieved from `firebaseUser.gerProviderData()`

- After signup with facebook login, linking email password login, then if email provider is unlinked, facebook email is not available 
any where in the user object.



