
(ns app.updater (:require [respo.cursor :refer [mutate]]))

(defn updater [store op op-data op-id op-time]
  (case op
    :states (update store :states (mutate op-data))
    :locales (assoc store :locales op-data)
    :pick-by (assoc store :pick-by op-data)
    :result (assoc store :result op-data)
    :missing (assoc store :missing op-data)
    :hydrate-storage op-data
    (do (js/console.warn "unknown op:" op) store)))
