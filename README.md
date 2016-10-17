## Sangria akka-http Example

An example [GraphQL](http://facebook.github.io/graphql/) server written with [akka-http](https://github.com/akka/akka-http) and [sangria](https://github.com/OlegIlyenko/sangria).

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

After starting the server with

    sbt run

you can run queries interactively using [GraphiQL](https://github.com/graphql/graphiql) by opening http://localhost:8080 in a browser or query the `/graphql` endpoint. It accepts following properties in the JSON body (this follows [relay](https://facebook.github.io/relay) convention):

* `query` - String - GraphQL query as a string
* `variables` - Object or String containing a JSON object that defines variables for your query _(optional)_
* `operationName` - String - the name of the operation, in case you defined several of them in the query _(optional)_

Here are some examples of the queries you can make:

```bash
$ curl -X POST localhost:8080/graphql \
    -H "Content-Type:application/json" \
    -d '{"query": "{hero {name, friends {name}}}"}'
```

this gives back the hero of StarWars Saga together with the list of his friends, which is of course R2-D2:

```javascript
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

```javascript
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
