[![Coupon CI/CD Pipeline](https://github.com/DavidPDP/coupon_engine/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/DavidPDP/coupon_engine/actions/workflows/ci-cd.yml)

# Metodología
## Modelo de Proceso
Para abordar el problema desde el ciclo de vida del desarrollo del software se procedió a utilizar el modelo de proceso en Cascada. Esto se debe a que el proyecto tiene un *scope* definido (sin continuación a evolución por parte del cliente), se trabaja sobre un problema de implementación cuyos requerimientos y tecnología es conocida. Por lo que un acercamiento sensato sería ir fase por fase hasta finalizar.

![](https://www.researchgate.net/profile/Desi-Suyamto/publication/327847933/figure/fig2/AS:674459493089286@1537815082824/The-waterfall-approach-in-software-engineering-Adapted-from-Dorfman-1997.ppm)

## Filosofía
Se hará uso de la filosofía DDD y de algunos principios de XP, más concretamente TDD/ATDD. En la primera parte se utilizará los conceptos de DDD para modularizar el sistema solución esto permitirá tener componentes más cohesivos de cara al contexto del negocio, dónde se desarrolla el problema. Segundo se utilizará TDD/ATDD definiendo un ciclo de iteración corto con el fin de que se pueda obtener un *feedback* expedito, que posteriormente genere artefactos testeables y consistentes a la lógica programada.
## Workflow
El flujo para la gestión de la configuración se realizará basado en GitHub Flow. Esto se debe a la simplicidad del versionamiento que se proyecta tener en el proyecto. También a que el equipo de trabajo es una sola persona, por lo que la colisión es improbable. De igual manera se mantiene una branch main como línea base de las versiones estables para producción y se utilizarán branches paralelos (features) para el desarrollo progresivo de las funcionalidades. Esto permitirá tener limpia la rama productiva con cambios listo para producción.

![Git Flow vs Github Flow](https://lucamezzalira.files.wordpress.com/2014/03/screen-shot-2014-03-08-at-23-07-36.png)

# Análisis
Utilizando la filosofía DDD y complementandola con la metodología de Merlín Dorfman, se procede a modular el sistema como se puede observar a continuación.

![Dorfman with DDD](https://drive.google.com/uc?export=download&id=1d3YPAbT1NCxLCQuo6m0iQZT0RSt8SfsV)

 - API: Contiene los flujos del sistema que expone el sistema solución.
 - Entities (Recommended Items y Meli Item): Entidades del dominio que gestionan el estado de la información necesaria para el procesamiento de los flujos.
 - Third-Parties: Integración con terceros por medio de HTTP. Encapsulando la lógica en el concepto de repositorio, ya que tiene la complejidad de la gestión para las colecciones de Meli Item.

Las estrategias serán implementadas siguiendo el patrón de diseño Strategy. Esta patrón se vuelve arquitectural (RAS). En el sentido que como sistema de recomendación pueden existir diferentes comportamientos que permitan definir un proceso de recomendación acorde a diferentes variables. Tomando esto, el patrón permite cumplir con los principios S.O.L.I.D en la medida que permite que el sistema pueda ser extendido en los acercamientos de recomendaciones sin afectar significativamente la estructura definida. 

![Strategy (patrón de diseño) - Wikipedia, la enciclopedia libre](https://upload.wikimedia.org/wikipedia/commons/3/32/Strategy_Pattern.jpg)

> Nota: Es importante aclarar que la firma del contrato del método calcular puede ser mejorada semánticamente por la entidad MELI Item, permitiendo extender diferentes propiedades en vez de coaptar a una propiedad en específico. 

# Diseño
Se identificaron los siguientes Requerimientos Arquitectónicamente Significativos:

 1. Performance (QA): Debido a la preocupación que se tiene por el volúmen de transacciones y la habilidad que debe tener el sistema para mantener la calidad con incrementos de cargas de trabajo. Esto último lo podemos sintetizar en Escalabilidad pero tomando como referencia a Len Bass, se abstrae todo como Performance.
 2. Proceso de recomendaciones (VF): Debido a la oportunidad como negocio de brindar dependiendo del contexto diferentes tipos de recomendaciones que permita una mayor conversión de compra por parte de los clientes.

Dado estos RAS se procede a proponer el diseño arquitectónico de la solución.

![Deployment](https://github.com/DavidPDP/coupon_engine/blob/main/docs/imgs/coupon-deployment%20.jpg)

Como se puede observar se procede a definir que la aplicación estará desplegada sobre un AWS Elastic Beanstalk directamente como instancia. Esto permite aprovechar las ventajas de auto-gestión del proveedor de nube. Se puede ver que no se hizo uso de virtualización (específicamente de Docker), esto se debe al tamaño de la aplicación. Por lo que en el nodo de procesamiento no hará falta aislar dependencias y precisamente procesos. Por lo que implementar Docker sería nada más un plus innecesario.

# Implementación
Tomando como base el diseño, direccionando las preocupaciones principales y teniendo en cuenta que la calidad no solo se garantiza en la fase de diseño, se procedió a implementar la lógica en 3 fases sintetizadas en 3 Issues (https://github.com/DavidPDP/coupon_engine/issues?q=is%3Aissue+is%3Aclosed). 

Como herramientas se procedió a utilizar Java como lenguaje de programación, teniendo en cuenta que es el lenguaje que utiliza mayoritariamente el cliente. Se utilizó el proyecto Spring: Spring Core, Spring Boot (como inicializador) y Spring WebFlux. Aunque una alternativa podría ser el web framework Spark para el problema específico Spring ofrece mayor agilidad en la configuración de múltiples preocupaciones.

Se utilizó reactividad para redireccionar el problema del performance, creando el flujo del sistema no bloqueante. Por último, se puede observar cada una de las iteracciones realizadas en la diferentes versiones lanzadas, en las que se puede ver como de manera oportuna se pudo ir iterando sobre el código de la mano con las pruebas. Lo que permite la consistencia y la habilitación de la automatización en el flujo de entrega (Continous Delivery).

> Nota: En la implementación del API, se decidió no seguir la guía respecto a la respuesta 404 cuando no hay items a recomendar. Esto se debe particularmente a la separación de las preocupaciones. HTTP es un protocolo de comunicación y es independiente de la capa de negocio. Un 404 significa un error de comunicación más no un error en el modelo de negocio. Por lo tanto la respuesta cuando no encuentra items es lista vacía y float = 0.00F. 

Se propuso un caché para la preocupación del performance (en varios niveles: red y lógico). Pero se desistió de la idea debido a que no tenemos un contexto claro del comporamiento de los usuarios vs el comportamiento de los vendedores. Durante el desarrollo se pudo evidenciar como los precios de varios items cambiaron, por lo que hay un alto riesgo sin tener contexto de cometer validación de caché.

## Resultados Pruebas
A continuación, se puede observar el resultados de las pruebas:

![Tests Class](https://github.com/DavidPDP/coupon_engine/blob/main/docs/imgs/tests-class.PNG)

![Test Coverage](https://github.com/DavidPDP/coupon_engine/blob/main/docs/imgs/test-coverage.PNG)

![Load Tests](https://user-images.githubusercontent.com/9300406/165667270-83d5e2fc-e6e9-4807-a639-18aa759d90a5.png)

Se revisa en la consola y se encuentra que hay un cuello de botella en los tiempos de respuestas y carga que maneja el API de MELI. Esto promueve un análisis detallado sobre cada una de las partes involucradas (aunque inicialmente ya se reconoció el cuello de botella que hay con la integración). Esto se podría completar con APM para sacar los perfiles de la aplicación y proponer soluciones estructurales al problema. Por último, teóricamente resiste la carga de 100K rpm.

# Despliegue
## Stand-Alone (local)
Para ejecutar desde el equipo local basta con clonar el repositorio y acceder al directorio resultante (coupon_engine):

    git clone https://github.com/DavidPDP/coupon_engine.git

Construir el proyecto con el Gradle Wrapper:

    ./gradlew clean build

Por último ejecutar la tarea que levanta el proceso de Spring:

    ./gradlew bootRun

## AWS EB
Se procedió automatizar el flujo del despliegue tomando dos caminos. El primero enfocado en la infraestructura (GitOps), el cual se trabajó con la herramienta Terraform, lo que permite la consistencia a la hora de recrear la infraestructura y la independencia del proveedor de nube para desplegar. En el segundo camino se procedió a definir un pipeline de despliegue por medio de GitHub Actions, lo que permite automatizar el despliegue de la infraestructura.

![Github Workflow](https://drive.google.com/uc?export=download&id=1gT4ZFuLC8mK1LGAaagoN1YzcqB6hkx04)

En la carpeta infra se encuentra el archivo main.tf con las instrucciones para recrear la infraestructura utilizada para el despliegue. Para recrearla basta con:

* Ejecutar desde la CLI de AWS (puede instalar desde este [link](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)) el siguiente comando. Esto le permitirá configurar las credenciales (puede crearlas desde la consola de IAM para setearlas localmente):
 
``` 
aws configure
```
    
* Una vez configurada (se creará los archivos correspondientes desde el /home/.aws) podrá ejecutar el archivo con Terraform siguiendo los pasos: 

```
terraform init
terraform validate
terraform plan
terraform apply
```

Para el pipeline de despliegue (Github Actions) se tendrá que crear un pull request para poder activarlo. Esto permite llevar una guarda sobre la calidad de la rama principal.

## Cloud
Para consumir desde la web se puede acceder por medio de la colección de Postman que se encuentra en la carpeta docs de este repositorio. El link de consumo es el siguiente: 

    http://coupon-engine-app.eba-2c4tzz2w.sa-east-1.elasticbeanstalk.com/coupon
    
[Local Postman Collection](https://github.com/DavidPDP/coupon_engine/blob/main/docs/postman/Coupon%20Engine%20Local.postman_collection.json)

[Cloud Postman Collection](https://github.com/DavidPDP/coupon_engine/blob/main/docs/postman/Coupon%20Engine%20Cloud.postman_collection.json)

Se puede también consumir por medio de un Curl:

    curl --location --request POST 'http://coupon-engine-app.eba-2c4tzz2w.sa-east-1.elasticbeanstalk.com/coupon' \
    --header 'Content-Type: application/json' \
    --data '{
        "items_id": ["MCO808833794","MCO808833795","MCO808833796","MCO808833797"],
        "amount": 50
    }'

## Open API 3.0 / Swagger
Puede consultar la documentación del API en cualquiera de los dos formatos:

Open API 3.0:
    
    http://localhost:5000/v3/api-docs
    
Swagger:

    http://localhost:5000/webjars/swagger-ui/index.html

# Gestión del Tiempo

![Time Distribution](https://user-images.githubusercontent.com/9300406/165649197-f6eeaa3a-205a-4053-8f39-ced67081c8ab.png)

# Gestión del Workflow de Configuración

![Workflow](https://user-images.githubusercontent.com/9300406/165650834-05a43a51-050b-4a9e-a55c-19d3a527b279.png)

