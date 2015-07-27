## Sangria akka-http Example

An example [GraphQL](http://facebook.github.io/graphql/) server written with [akka-http](http://doc.akka.io/docs/akka-stream-and-http-experimental/current/scala/http/) and [sangria](https://github.com/OlegIlyenko/sangria).

After starting the server with

    sbt run

you can query `/graphql` endpoint. It accepts following query string parameters:

* `query` - GraphQL query as a string
* `args` - JSON object that contains variables for your query _(optional)_
* `operation` - the name of the operation, in case you defined several of them in the query _(optional)_

Here are some examples of the queries you can make:

```bash
$ curl -G localhost:8080/graphql \
    --data-urlencode "query={hero {name, friends {name}}}"
```

this give back the hero of StarWars Saga together with his friends, which is of course R2-D2:

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
$ curl -G localhost:8080/graphql \
    --data-urlencode "query=query Test(\$humanId: String){human(id: \$humanId) {name, homePlanet, friends {name}}}" \
    --data-u rlencode 'args={"humanId": "1000"}'
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