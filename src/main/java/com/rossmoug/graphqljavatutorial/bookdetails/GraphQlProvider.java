package com.rossmoug.graphqljavatutorial.bookdetails;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQlProvider {

  private GraphQL graphQl;

  @Bean
  public GraphQL graphQl() {
    return graphQl;
  }

  @Autowired
  private GraphQlDataFetchers graphQlDataFetchers;

  @PostConstruct
  public void init() throws IOException {
    final URL url = Resources.getResource("schema.graphqls");
    final String sdl = Resources.toString(url, Charsets.UTF_8);
    final GraphQLSchema graphQlSchema = buildSchema(sdl);
    this.graphQl = GraphQL.newGraphQL(graphQlSchema)
                          .build();
  }

  private GraphQLSchema buildSchema(String sdl) {
    final TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(sdl);
    final RuntimeWiring runtimeWiring = buildWiring();
    final SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
  }

  private RuntimeWiring buildWiring() {
    return RuntimeWiring.newRuntimeWiring()
                        .type(newTypeWiring("Query")
                            .dataFetcher("bookById", graphQlDataFetchers.getBookByIdDataFetcher()))
                        .type(newTypeWiring("Book")
                            .dataFetcher("author", graphQlDataFetchers.getAuthorDataFetcher()))
                        .build();
  }
}
