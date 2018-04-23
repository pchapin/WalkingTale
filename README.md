# Walking Tale

## What is it?
Walking Tale is an android app that allows users
to easily create location based content and share it with other people.

The app allows users to upload audio, pictures, video, and text, which all show
up as markers on a map.

Walking Tale was made by four seniors at 
[Vermont Tech](https://www.vtc.edu/) for a graduation project.

## What tech does it use?
On the backend, several AWS services are used. A DynamoDb table stores the posts,
an S3 bucket stores the media, and a Cognito user pool authenticates users.

On the android side, multiple android architecture components are used.
Room stores posts in a local db, Viewmodels hold on to data that should survive
lifecycle changes, and Livedata is used to observe data as it changes.

We also took advantage of the following open-source code:

[Google map marker clustering utility](https://developers.google.com/maps/documentation/android-api/utility/marker-clustering)

This was a great solution to the problem: "I have too many things to display on a map at once."
We didn't change much at all.

[Github browser sample](https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample)

This was used as a template to start the project from. It was quite handy because it
already had a repository class set up to make http requests. Walking tale at first used an AWS api gateway
which retrofit would make calls to, but eventually we moved to the Dynamodb mapper sdk, which 
is way more concise.

[Custom Cognito Sign-in](https://github.com/mimurawil/android-aws-mobilehub-login-model)

The [default sign](https://docs.aws.amazon.com/aws-mobile/latest/developerguide/add-aws-mobile-user-sign-in.html) 
provided by AWS was pretty nice, but it was not very customizable,
so this open source version worked well.

## Setup

A few files are not in version control which are needed to build.
These are:

- awsconfiguration.json
This belongs in the raw directory. This was generated using AWS mobile hub.

- google_maps_api.xml 
This is a resource file that contains our Google maps api key as well as a few
constants that should stay private.

- google-services.json
This belongs in the app directory.

## Authors

[Todd Cooke](https://toddcooke.github.io/)

Jonathan Broadfoot

Amina Karic

Patrick Wilkins