package org.wso2.carbon.test;

import com.intuit.graphql.orchestrator.GraphQLOrchestrator;
import com.intuit.graphql.orchestrator.schema.RuntimeGraph;
import com.intuit.graphql.orchestrator.stitching.SchemaStitcher;
import graphql.ExecutionInput;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionStrategy;
import graphql.schema.idl.SchemaPrinter;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import java.util.Map;
import graphql.validation.rules.

public class customAuthenticationHandler extends AbstractHandler {

    private static final HttpClient httpClient = HttpClients.createDefault();
    private ExecutionInput executionInput;
    private static Map<String, Object> executionResult;

    public boolean handleRequest(MessageContext messageContext) {

        ExecutionStrategy queryExecutionStrategy = new AsyncExecutionStrategy();

        try {

            System.out.println("WE ARE STARTINGGG!!!");

            String inventorySchema = "type Product @key(fields: \"upc\") @extends {\n" +
                    "    upc: String! @external\n" +
                    "    weight: Int @external\n" +
                    "    price: Int @external\n" +
                    "    inStock: Boolean\n" +
                    "    shippingEstimate: Int @requires(fields: \"price weight\")\n" +
                    "}";
            String productSchema = "type Query {\n" +
                    "    topProducts(first: Int = 5): [Product]\n" +
                    "    productB (upc : String!): Product\n" +
                    "}\n" +
                    "\n" +
                    "type Product @key(fields: \"upc\") {\n" +
                    "    upc: String!\n" +
                    "    name: String\n" +
                    "    price: Int\n" +
                    "    weight: Int\n" +
                    "}\n";
            String reviewSchema = "type Review @key(fields: \"id\") {\n" +
                    "    id: ID!\n" +
                    "    body: String\n" +
                    "    product: Product @resolver(field:\"productB\", arguments: [{name : \"upc\", value: \"UPC001\"}])\n" +
                    "}\n" +
                    "\n" +
                    "type User @key(fields: \"id\") @extends {\n" +
                    "    id: ID! @external\n" +
                    "    username: String @external\n" +
                    "    reviews: [Review]\n" +
                    "}\n" +
                    "\n" +
                    "type Product @key(fields: \"upc\") @extends {\n" +
                    "    upc: String! @external\n" +
                    "    reviews: [Review]\n" +
                    "}\n" +
                    "\n" +
                    "# ================================\n" +
                    "# define this as built-in directive\n" +
                    "directive @resolver(field: String!, arguments: [ResolverArgument!]) on FIELD_DEFINITION\n" +
                    "\n" +
                    "# define this as built-in type\n" +
                    "input ResolverArgument {\n" +
                    "    name : String!\n" +
                    "    value : String!\n" +
                    "}";
            String userSchema = "type Query {\n" +
                    "    me: User\n" +
                    "}\n" +
                    "\n" +
                    "type User @key(fields: \"id\") {\n" +
                    "    id: ID!\n" +
                    "    name: String\n" +
                    "    username: String\n" +
                    "}";

            // Creating providers for your GraphQL services
            GenericProvider inventoryService = new GenericProvider("http://localhost:8084/graphql", httpClient,
                    inventorySchema, "inventory");
            GenericProvider productService = new GenericProvider("http://localhost:8081/graphql", httpClient,
                    productSchema, "product");
            GenericProvider reviewService = new GenericProvider("http://localhost:8083/graphql", httpClient,
                    reviewSchema, "review");
            GenericProvider accountService = new GenericProvider("http://localhost:8082/graphql", httpClient,
                    userSchema, "user");

            System.out.println(reviewService.getNameSpace());
            System.out.println(accountService.getNameSpace());
            System.out.println(inventoryService.getNameSpace());
            System.out.println(productService.sdlFiles());

            RuntimeGraph runtimeGraph = SchemaStitcher.newBuilder()
                    .service(accountService)
                    .service(productService)
                    .service(inventoryService)
                    .service(reviewService)
                    .build()
                    .stitchGraph();

            String printSchema = new SchemaPrinter().print(runtimeGraph.getExecutableSchema());
            System.out.println(printSchema);

            GraphQLOrchestrator.Builder builder = GraphQLOrchestrator.newOrchestrator();
            builder.runtimeGraph(runtimeGraph);
            builder.queryExecutionStrategy(queryExecutionStrategy);
            GraphQLOrchestrator graphQLOrchestrator = builder.build();

            ExecutionInput.Builder eiBuilder = ExecutionInput.newExecutionInput();
            eiBuilder.query("{\n" +
                    "  topProducts {\n" +
                    "    name\n" +
                    "    reviews {\n" +
                    "      product {\n" +
                    "        inStock\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "  me {\n" +
                    "    name\n" +
                    "  }\n" +
                    "} \n");

            ExecutionInput executionInput = eiBuilder.build();

            executionResult = graphQLOrchestrator.execute(executionInput).get().toSpecification();

            System.out.println(executionResult);

        }catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        System.out.println("response:Shamin");
        return true;
    }
}
