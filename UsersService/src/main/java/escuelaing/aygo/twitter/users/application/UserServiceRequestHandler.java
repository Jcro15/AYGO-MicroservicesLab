package escuelaing.aygo.twitter.users.application;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import escuelaing.aygo.twitter.users.domain.User;
import escuelaing.aygo.twitter.users.domain.UserService;
import escuelaing.aygo.twitter.users.domain.UserServiceException;
import escuelaing.aygo.twitter.users.utils.RequestMapping;
import escuelaing.aygo.twitter.users.utils.RequestMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.HashMap;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserServiceRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    private final UserService userService = new UserService();

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
                    Pattern pattern = Pattern.compile("^" + routePath.replaceAll("\\{\\w+\\}", "(.+)") + "$");
                    Matcher matcher = pattern.matcher(request.getPath());

                    if (matcher.matches()) {
                        try {
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

    @RequestMapping(path = "/users", method = RequestMethod.POST)
    public APIGatewayProxyResponseEvent saveUser(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        LambdaLogger logger = context.getLogger();

        try {
            var user = objectMapper.readValue(request.getBody(), User.class);
            var createdUser = userService.saveUser(user);
            return buildJsonResponse(201, objectMapper.writeValueAsString(createdUser));
        } catch (UserServiceException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(409).withBody(e.getMessage());
        }

    }

    @RequestMapping(path = "/users/{userId}", method = RequestMethod.GET)
    public APIGatewayProxyResponseEvent getUser(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        String userId = request.getPathParameters().get("userId");
        Optional<User> user = userService.getUserById(userId);
        if (user.isPresent()) {
            return buildJsonResponse(200, objectMapper.writeValueAsString(user.get()));
        } else {
            return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody("User " + userId + " not found");
        }
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public APIGatewayProxyResponseEvent getAllUsers(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        return buildJsonResponse(200, objectMapper.writeValueAsString(userService.getAll()));
    }
    @RequestMapping(path = "/users/{userId}", method = RequestMethod.DELETE)
    public APIGatewayProxyResponseEvent deleteUser(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        String userId = request.getPathParameters().get("userId");
        try{
            userService.deleteUserById(userId);
        }catch (UserServiceException e){
            new APIGatewayProxyResponseEvent().withStatusCode(404).withBody(e.getMessage());
        }
        return new APIGatewayProxyResponseEvent().withStatusCode(204);
    }


    private APIGatewayProxyResponseEvent buildJsonResponse(int statusCode, String body) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new APIGatewayProxyResponseEvent().withBody(body).withIsBase64Encoded(false).withStatusCode(statusCode).withHeaders(headers);
    }
}