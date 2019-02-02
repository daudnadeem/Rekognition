package com.amazonaws.lambda.demo;

import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class LambdaFunctionHandler implements RequestHandler<S3Event, String> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("eu-west-1").build();

    public LambdaFunctionHandler() {}

    // Test purpose only.
    LambdaFunctionHandler(AmazonS3 s3) {
        this.s3 = s3;
    }

    // Basically get the latest upload
    
    @Override
    public String handleRequest(S3Event event, Context context) {
        context.getLogger().log("Received event: " + event);

        // Get the object from the event and show its content type
        String bucket = event.getRecords().get(0).getS3().getBucket().getName();
        String key = event.getRecords().get(0).getS3().getObject().getKey();
        try {
            S3Object response = s3.getObject(new GetObjectRequest(bucket, key));
            String contentType = response.getObjectMetadata().getContentType();
            context.getLogger().log("CONTENT TYPE: " + contentType);
            //function to detect text
            DetectText(key, bucket);
            return contentType;
        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().log(String.format(
                "Error getting object %s from bucket %s. Make sure they exist and"
                + " your bucket is in the same region as this function.", key, bucket));
            throw e;
        }
       
    }
    
    
    //Detect text function
    
    public String DetectText(String photo, String bucketName) {

//        String photo = "Paracetamol.jpg";
//        String bucket = "boots-image-rekog";

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion("eu-west-1").build();
      		  //.defaultClient();

       
        
        DetectTextRequest request = new DetectTextRequest()
                .withImage(new Image()
                .withS3Object(new S3Object()
// Not sure why this does not work
                .withName(photo)
                .withBucket(bucketName)));
      

        try {
           DetectTextResult result = rekognitionClient.detectText(request);
           List<TextDetection> textDetections = result.getTextDetections();

           System.out.println("Detected lines and words for " + photo);
           for (TextDetection text: textDetections) {
        
                   System.out.println("Detected: " + text.getDetectedText());
                   System.out.println("Confidence: " + text.getConfidence().toString());
                   System.out.println("Id : " + text.getId());
                   System.out.println("Parent Id: " + text.getParentId());
                   System.out.println("Type: " + text.getType());
                   System.out.println();
           }
        } catch(AmazonRekognitionException e) {
           e.printStackTrace();
        }
     }
}