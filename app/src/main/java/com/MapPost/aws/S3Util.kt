package com.MapPost.aws

import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client

fun getTransferUtility(context: Context): TransferUtility {
    return TransferUtility.builder()
            .defaultBucket(s3BucketName)
            .awsConfiguration(AWSMobileClient.getInstance().configuration)
            .s3Client(AmazonS3Client(AWSMobileClient.getInstance().credentialsProvider))
            .context(context)
            .build()
}