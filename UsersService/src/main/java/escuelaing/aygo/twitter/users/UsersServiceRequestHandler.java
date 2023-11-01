package escuelaing.aygo.twitter.users;

import escuelaing.aygo.twitter.users.domain.User;
import escuelaing.aygo.twitter.users.utils.RequestMapping;
import escuelaing.aygo.twitter.users.utils.RequestMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsersServiceRequestHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HashMap<Long,User> memory = new HashMap<>();
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
                    Pattern pattern = Pattern.compile("^" + routePath.replaceAll("\\{\\w+\\}", "(\\\\w+)") + "$");
                    Matcher matcher = pattern.matcher(request.getPath());

                    if (matcher.matches()) {
                        try {
                            return (APIGatewayProxyResponseEvent) method.invoke(this, request, context);
                        } catch (Exception e) {
                            logger.log(e.getMessage());
                            logger.log(Arrays.toString(e.getStackTrace()));
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
        User user = objectMapper.readValue(request.getBody(), User.class);
        memory.put(user.getUserId(),user);

        return buildJsonResponse(200,objectMapper.writeValueAsString(user));
    }
    @RequestMapping(path = "/users/{userId}", method = RequestMethod.GET)
    public APIGatewayProxyResponseEvent getUser(APIGatewayProxyRequestEvent request, Context context) throws Exception {
        LambdaLogger logger = context.getLogger();

        long userId = Long.parseLong(request.getPathParameters().get("userId"));

        return buildJsonResponse(200, objectMapper.writeValueAsString(memory.get(userId)));

    }

    private APIGatewayProxyResponseEvent buildJsonResponse(int statusCode, String body) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return new APIGatewayProxyResponseEvent()
                .withBody(body)
                .withIsBase64Encoded(false)
                .withStatusCode(statusCode)
                .withHeaders(headers);
    }
}