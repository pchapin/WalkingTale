# Walking Tale

## What is it?

Walking Tale is an android app that allows users
to easily create location based content and share it with other people.

The app allows users to upload audio, pictures, video, and text, which all show
up as markers on a map.

## What tech does it use?

On the backend, several AWS services are used. A DynamoDb table stores the posts,
an S3 bucket stores the media, and a Cognito user pool authenticates users.

On the frontend android side, multiple android architecture components are used.
Room stores posts in a local db, Viewmodels hold on to data that should survive
lifecycle changes, and Livedata is used to observe data as it changes.

## Authors
Todd Cooke
Jonathan Broadfoot
Amina Karic
Patrick Wilkins