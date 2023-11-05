# AYGO-MicroservicesLab : prototipo Twiteer

El objetivo de este laboratorio es construir un prototipo de la red social Twitter ( ahora X),
el alcance de estre prototipo está limitado a permitir a los usuarios publicar y consultar tweets. El
objetivo es explorar las capacidades de las arquitecturas masivamente distribuidas, además de servicios de AWS
como API Gateway, Lambda y EC2.

## Identificación de recursos
A partir del alcance definido para el prototipo, se identificaron 2 recursos: Usuarios y Tweets
### Representación de los recursos
A continuación se presenta la representación en el dominio que se usará para cada recurso, adicionalmente
la API representará estos objetos utilizando el formato JSON

![domainModel.png](img%2FdomainModel.png)

### Métodos soportados
A continuación se listan los métodos soportados, junto con las rutas y métodos HTTP definidos para cada uno de los
recursos

**Recurso Usuarios :**

| Método HTTP | Path            | Descripción                                              |
|-------------|-----------------|----------------------------------------------------------|
| POST        | /users          | Registrar un nuevo usuario en la aplicación              |
| GET         | /users          | Obtener la información de todos los usuarios registrados |
| GET         | /users/{userId} | Obtener la información del usuario con el id `userId`    |
| PUT         | /users/{userId} | Actualizar la información del usuario con el id `userId` |
| DELETE      | /users/{userId} | Eliminar al usuario con el id `userId`                   |

**Recurso Tweets:**

| Método HTTP | Path                  | Descripción                                                        |
|-------------|-----------------------|--------------------------------------------------------------------|
| POST        | /tweets               | Registrar un nuevo tweet en la aplicación                          |
| GET         | /tweets               | Obtener la información de todos los tweets registrados             |
| GET         | /tweets/{tweetId}     | Obtener la información del tweet con el id `tweetId`               |
| PUT         | /tweets/{tweetId}     | Actualizar la información del tweet con el id `tweetId`            |
| DELETE      | /tweets/{tweetId}     | Eliminar el tweet con el id `tweetId`                              |
| GET         | /tweets/user/{userId} | Obtener todos los tweets creados por el usuario con el id `userId` |


## Arquitectura de la aplicación
La arquitectura diseñada para implementar los recursos identificados se muestra a continuación

![awsArchitecture.png](img%2FawsArchitecture.png)

En esta arquitectura se puede destacar el uso de API gateway como punto de entrada de la aplicación,
este servicio redirecciona las peticiones hacia dos lambdas, UsersService y TweetService , dependiendo la ruta
de la solicitud recibida. El api gateway redirige las peticiones haciendo proxy a las lambdas, que se encargan
de resolver la ruta y el método solicitado y ejecutar la acción correspondiente.

Para persistir la información se utilizan tablas de Dynamo, una por cada microservicio.

Adicionalmente, para validar las entradas de los endpoints en los servicios, se hace uso de los Models de API Gateway para
asegurar un formato de entrada válido.


## Despliegue
El prototipo se desplegó en AWS siguiendo la arquitectura propuesta, la API se puede consultar utilizando
la siguiente URL: https://7cdi34eov2.execute-api.us-east-1.amazonaws.com/v1

Para probar los endpoints desarrollados, puede utilizar el archivo [Twitter POC-v1-oas30.yaml](Twitter%20POC-v1-oas30.yaml) para
importarlo como una especificación OpenApi V3 utilizando herramientas como postman, o puede acceder al siguiente
[Swagger Online](https://petstore.swagger.io/?url=https://github.com/Jcro15/AYGO-MicroservicesLab/blob/main/Twitter%20POC-v1-oas30.yaml#/default/get_users), 
simplemente asegurese de desactivar las validaciones CORS del navegador para probar las peticiones