# OAuth support for Ring server applications in Clojure #

`clj-oauth` provides [OAuth](http://oauth.net) Server support for Clojure programs.

The server support makes it simple to add OAuth support to any [Ring](http://github.com/mmcgrana/ring) based web applications such as Compojure.

# Building #

`lein jar`

The server support is implemented as Ring middleware. It depends on params middleware already having been run upstream.

# Authors #

I forked this from [clj-oauth](http://www.github.com/mattrepl/clj-oauth).
