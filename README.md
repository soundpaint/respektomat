# respektomat -- An Interactive Arts Object Installation

»Respektomat« is an arts project devised as contribution for the 2019
member exhibition of Poly Produzentengalerie e.V., Karlsruhe, Germany,
on December 6, 2019.  The exhibition is titled »Respekt«.

»Respektomat« is a text console based, interactive program for
discussing the topic with the computer.  The technology behind it
somewhat resembles concepts of good old Joseph Weizenbaum's »ELIZA«[1]
processor.  Still, there are some significant differences:

* While »ELIZA« uses a setting with psychotherapist to talk to, the
  »Respektomat« is rather an interactive information system and talk
  bot on the specific topic of "respect".

* While »ELIZA« tries in many cases to create sentences by pattern
  matching and replacing from the user's input, »Respektomat« instead
  analyzes the user's input for searching for the best matching
  statement from a big database to be used as a response to the user's
  input.

* While »ELIZA« is based on English language and heavily depends on
  its grammatical structure, »Respektomat« uses German as language,
  but should be portable to other languages with minimal effort.
  Note, for example, that »ELIZA« depends on constructing subclauses
  from a main clause.  In English language, this is relatively easy.
  For example, you can take the main clause "today it is raining" and
  turning it into a subclause by prepending e.g. the word "if": "if
  today it is raining, then …".  In German language, this is not
  directly possible, but the sentence must be rephrased.  For example,
  the main clause "heute regnet es", it must be rephrased as "falls es
  heute regnet, dann …" when prepending the conditional word "falls".

== Bibliography ==

[1] Wikipedia: _ELIZA_.  URL: https://en.wikipedia.org/wiki/ELIZA
(last visited on: 2019-11-17).
