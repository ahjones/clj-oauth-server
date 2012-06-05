# OAuth support for Ring server applications in Clojure #

`clj-oauth-server` provides [OAuth](http://oauth.net) Server support for Clojure programs.

The server support makes it simple to add OAuth support to any [Ring](http://github.com/mmcgrana/ring) based web applications such as Compojure.

# Building #

`lein jar`

The server support is implemented as Ring middleware. It depends on params middleware already having been run upstream.

# Use #

`clj-oauth-server` is in [Clojars](https://clojars.org/clj-oauth-server). Use it with Leiningen by addit to `project.clj`

    [clj-oauth-server "1.0.0"]

Add it to your application. Ensure that ring parameter middleware is before `clj-oauth-server`.

    (def app (-> routes
                 (oauth/wrap-oauth token-finder)
                 handler/api))

`token-finder` is a function that looks up consumer secret and token secret given consumer key and token.

    (defn token-finder [consumer token]
      ["consumer_secret" "token_secret"])

This would always match.

# Authors #

I forked this from [clj-oauth](http://www.github.com/mattrepl/clj-oauth).
