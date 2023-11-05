package escuelaing.aygo.twitter.tweets.application;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import escuelaing.aygo.twitter.tweets.domain.NoAccessException;
import escuelaing.aygo.twitter.tweets.domain.Tweet;
import escuelaing.aygo.twitter.tweets.domain.TweetService;
import escuelaing.aygo.twitter.tweets.domain.TweetServiceException;
import escuelaing.aygo.twitter.tweets.utils.RequestMapping;
import escuelaing.aygo.twitter.tweets.utils.RequestMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweetServiceRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    private final TweetService tweetService = new TweetService();
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        LambdaLogger logger = context.getLogger();
        Method[] methods = this.getClass().getMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping routeAnnotation = method.getAnnotation(RequestMapping.class);
                String routePath = routeAnnotation.path();
                RequestMethod routeMethod = routeAnnotation.method();

                if (request.getHttpMethod().equals(routeMethod.name())) {
                    Pattern pattern = Pattern.compile("^" + routePath.replaceAll("\\{\\w+\\}", "([^/]+)") + "$");
                    Matcher matcher = pattern.matcher(request.getPath());

                    if (matcher.matches()) {
                        try {
                            System.out.println("executing:"+method.getName());
                            return (APIGatewayProxyResponseEvent) method.invoke(this, request, context);
                        } catch (Exception e) {
                            logger.log(e.getMessage());
                            logger.log(e.getLocalizedMessage());
                            var message = "Internal Server Error";
                            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(message);
                        }
                    }
                }
            }
        }

        // Handle other endpoints or return a 404 response
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(404);
        response.setBody("Not Found");

        return response;
    }

    @RequestMapping(path = "/tweets", method = RequestMethod.POST)
    public APIGatewayProxyResponseEvent saveTweet(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        var tweet = objectMapper.readValue(request.getBody(), Tweet.class);
        var createdTweet = tweetService.saveTweet(tweet);

        return buildJsonResponse(201, objectMapper.writeValueAsString(createdTweet));
    }

    @RequestMapping(path = "/tweets/{tweetId}", method = RequestMethod.GET)
    public APIGatewayProxyResponseEvent getTweetById(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        String tweetId = request.getPathParameters().get("tweetId");
        Optional<Tweet> user = tweetService.getTweetById(tweetId);
        if (user.isPresent()) {
            return buildJsonResponse(200, objectMapper.writeValueAsString(user.get()));
        } else {
            return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody("Tweet " + tweetId + " not found");
        }
    }

    @RequestMapping(path = "/tweets", method = RequestMethod.GET)
    public APIGatewayProxyResponseEvent getAllTweets(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        return buildJsonResponse(200, objectMapper.writeValueAsString(tweetService.getAll()));
    }

    @RequestMapping(path = "/tweets/user/{userId}", method = RequestMethod.GET)
    public APIGatewayProxyResponseEvent getAllTweetsByUserId(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        LambdaLogger logger = context.getLogger();
        String userId = request.getPathParameters().get("userId");
        return buildJsonResponse(200, objectMapper.writeValueAsString(tweetService.getAllByUserId(userId)));


    }
    @RequestMapping(path = "/tweets/{tweetId}", method = RequestMethod.DELETE)
    public APIGatewayProxyResponseEvent deleteTweet(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        String tweetId = request.getPathParameters().get("tweetId");
        try{
            tweetService.deleteTweetById(tweetId);
            return new APIGatewayProxyResponseEvent().withStatusCode(204);
        }catch (TweetServiceException e){
           return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody(e.getMessage());
        }
    }
    @RequestMapping(path = "/tweets/{tweetId}", method = RequestMethod.PUT)
    public APIGatewayProxyResponseEvent updateTweet(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        try {
            String tweetId = request.getPathParameters().get("tweetId");
            var tweet = objectMapper.readValue(request.getBody(), Tweet.class);
            var updatedTweet = tweetService.updateTweet(tweetId,tweet);
            return buildJsonResponse(200, objectMapper.writeValueAsString(updatedTweet));
        }
        catch (TweetServiceException e){
            return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody(e.getMessage());
        }
        catch (NoAccessException e){
            return new APIGatewayProxyResponseEvent().withStatusCode(403).withBody(e.getMessage());
        }
    }


    private APIGatewayProxyResponseEvent buildJsonResponse(int statusCode, String body) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new APIGatewayProxyResponseEvent().withBody(body).withIsBase64Encoded(false).withStatusCode(statusCode).withHeaders(headers);
    }

}
