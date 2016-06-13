(ns ga4gh.reference-api.utils
  (:require
   [cljs.nodejs :as nodejs]
   cljs.pprint
   ))


(defn log [& args]
  (doseq [x args]
    (.log js/console x))
  (last args))


(defn cljslog [& args]
  (apply log (map #(with-out-str (cljs.pprint/pprint %)) args))
  (last args))


(defn log-error [& args]
  (doseq [x args]
    (.error js/console x))
  (last args))


(def default-headers {"Access-Control-Allow-Origin" "*"})


(defn- write-head
  ([ctx status] (write-head ctx status nil))
  ([ctx status headers]
   (.writeHead (:res ctx) status (clj->js (merge default-headers headers)))))


(defn- end
  ([ctx] (.end (:res ctx)))
  ([ctx body] (.end (:res ctx) body)))


(defn respond
  ([ctx status] (respond ctx status nil))
  ([ctx status body]
   (write-head ctx status)
   (if body (end ctx body) (end ctx))))


(defn respond-with-headers
  ([ctx status headers] (respond-with-headers ctx status headers nil))
  ([ctx status headers body]
   (write-head ctx status headers)
   (if body (end ctx body) (end ctx))))


(defn respond-with-json [ctx status x]
  (respond-with-headers
   ctx status {"Content-Type" "application/json"}
   (str (.stringify js/JSON (clj->js x) nil 2) "\n")))


(defn collect-body [ctx f]
  (let [req (-> ctx :req)
        data (atom "")]
    (.on req "data" (fn [chunk] (swap! data str chunk)))
    (.on req "end" (fn [] (f (assoc ctx :request-body {:content @data}))))))


(defn parse-json [ctx f]
  (collect-body
   ctx
   (fn [ctx]
     (let [parsed (try
                    (.parse js/JSON (-> ctx :request-body :content))
                    (catch js/SyntaxError e :syntax-error))]
       (case parsed
         :syntax-error (respond-with-json ctx 400 {:error "syntax-error"
                                                   :message "Could not parse request body as JSON"})
         (f (assoc-in ctx [:request-body :parsed] (js->clj parsed))))))))
