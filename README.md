## Sangria akka-http Example

An example [GraphQL](https://graphql.org) server written with [akka-http](https://github.com/akka/akka-http), [circe](https://github.com/circe/circe) and [sangria](https://github.com/sangria-graphql/sangria).

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

After starting the server with

```bash
sbt run

# or, if you want to watch the source code changes
 
sbt ~reStart
``` 

you can run queries interactively using [graphql-playground](https://github.com/prisma/graphql-playground) by opening [http://localhost:8080](http://localhost:8080) in a browser or query the `/graphql` endpoint directly. The HTTP endpoint follows [GraphQL best practices for handling the HTTP requests](http://graphql.org/learn/serving-over-http/#http-methods-headers-and-body).

Here are some examples of the queries you can make:

```bash
$ curl -X POST localhost:8080/graphql \
  -H "Content-Type:application/json" \
  -d '{"query": "{hero {name, friends {name}}}"}'
```

this gives back the hero of StarWars Saga together with the list of his friends, which is of course R2-D2:

```json
{
  "data": {
    "hero": {
      "name": "R2-D2",
      "friends": [
        {
          "name": "Luke Skywalker"
        },
        {
          "name": "Han Solo"
        },
        {
          "name": "Leia Organa"
        }
      ]
    }
  }
}
```

Here is another example, which uses variables:

```bash
$ curl -X POST localhost:8080/graphql \
  -H "Content-Type:application/json" \
  -d '{"query": "query Test($humanId: String!){human(id: $humanId) {name, homePlanet, friends {name}}}", "variables": {"humanId": "1000"}}'
```

The result should be something like this:

```json
{
  "data": {
    "human": {
      "name": "Luke Skywalker",
      "homePlanet": "Tatooine",
      "friends": [
        {
          "name": "Han Solo"
        },
        {
          "name": "Leia Organa"
        },
        {
          "name": "C-3PO"
        },
        {
          "name": "R2-D2"
        }
      ]
    }
  }
}
```
