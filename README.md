# GraphQLFederation-CustomHandler
Custom Handler to test out GraphQL Federation in wso2 API-Managerr

# Running the API-M with the custom Handler 

1. First Clone the [GraphQLFederation Orbit](https://github.com/shamin2021/GraphQLFederation-Orbit) and open in IDE and run a ```mvn clean install``` to build a jar file
2. Open the GraphQLFederation-CustomHandler in IDE and run a ```mvn clean install``` to build a jar file
3. After the JAR is created include the GraphQLFederation-CustomHandler JAR file in the ```repository/components/dropins``` path of the API-M pack
4. Then navigate to ```repository/resources/api_templates``` open the velocity_template.xml file and add the following line ```<handler class="org.wso2.carbon.test.customAuthenticationHandler"/>``` after the ```<handlers xmlns="http://ws.apache.org/ns/synapse">```
5. After making the changes start the server
6. Deploy the sample pizza shack API from the publisher portal and invoke
7. You will be able to see the output in the terminal
    
# Necessary Documnets to Refer 
