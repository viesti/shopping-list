(ns shopping-list.middleware.session-timeout)

(defn wrap-session-timeout [handler session-timeout-secs]
  (fn [{:keys [session] :as request}]
    (let [now (System/currentTimeMillis)]
      (if (and (:identity session)
               (< (- now (:last-activity session)) session-timeout-secs))
        (let [response (handler request)]
          (assoc response :session (assoc (or (:session response) session) :last-activity now)))
        {:status 401
         :body "Session expired"
         :session nil}))))
