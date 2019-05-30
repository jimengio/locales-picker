
(ns app.vm
  (:require [clojure.string :as string]
            [app.config :refer [dev?]]
            [clojure.reader :refer [read-string]]
            [favored-edn.core :refer [write-edn]]
            ["copy-text-to-clipboard" :as copy!]))

(defn get-view-model [store] store)

(def state-container
  {:init (fn [props state] (or state {:locales-text "", :select-text "", :result ""})),
   :update (fn [d! op context options state mutate!]
     (let [grab! (fn []
                   (try
                    (let [locales (:locales (read-string (:locales-text state)))
                          entries (string/split (:select-text state) "\n")
                          chunks (select-keys locales entries)]
                      (mutate! (assoc state :result (write-edn chunks) :error nil)))
                    (catch
                     js/Error
                     e
                     (js/console.error e)
                     (mutate! (assoc state :error (str e))))))]
       (case op
         :locales (mutate! (assoc state :locales-text (:value options)))
         :keys (mutate! (assoc state :select-text (:value options)))
         :copy (copy! (:result state))
         :grab (grab!)
         :keydown
           (let [event (:event options)]
             (when (and (= 13 (.-keyCode event)) (.-metaKey event)) (grab!)))
         (do (println "Unknown op:" op)))))})

(def states-manager {"container" state-container})

(defn on-action [d! op context options view-model states]
  (let [param (:param options)
        template-name (:template-name context)
        state-path (:state-path context)
        mutate! (fn [x] (d! :states [state-path x]))
        this-state (get-in states (conj state-path :data))]
    (when dev? (println "Action" op param (pr-str options)))
    (if (contains? states-manager template-name)
      (let [action-handler (get-in states-manager [template-name :update])
            state-fn (get-in states-manager [template-name :init])
            state (if (fn? state-fn) (state-fn (:data context) this-state) this-state)]
        (action-handler d! op context options state mutate!))
      (println "Unhandled template:" template-name))))

(def state-header
  {:init (fn [props state] (or state {:draft ""})),
   :update (fn [d! op context options state mutate!]
     (case op
       :input (mutate! (assoc state :draft (:value options)))
       :submit
         (when-not (string/blank? (:draft state))
           (d! :submit (:draft state))
           (mutate! (assoc state :draft "")))
       (do (println "Unknown op:" op))))})
