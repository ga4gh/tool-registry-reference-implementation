(ns ga4gh.reference-api.testing)


(defmacro defswaggertest [test-name api-method-keyword args callback-fn]
  `(cljs.test/deftest ~test-name
     (test-request
      ~api-method-keyword
      ~args
      ~callback-fn)))
