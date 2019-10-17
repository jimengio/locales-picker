
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp cursor-> action-> list-> <> div button textarea span input]]
            [respo.comp.space :refer [=<]]
            [reel.comp.reel :refer [comp-reel]]
            [respo-md.comp.md :refer [comp-md]]
            [app.config :refer [dev?]]))

(def by-tabs [{:value :key, :display "Key"} {:value :zh-cn, :display "zh-CN"}])

(defcomp
 comp-tabs
 (tabs value on-change)
 (list->
  {:style ui/row-middle}
  (->> tabs
       (map
        (fn [tab]
          [(:value tab)
           (div
            {:style (merge
                     {:padding "0 8px", :cursor :pointer}
                     (if (= value (:value tab))
                       {:border-bottom (str "1px solid " (hsl 0 0 80))})),
             :on-click (fn [e d! m!] (on-change tab d! m!))}
            (<> (:display tab)))])))))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel), states (:states store)]
   (div
    {:style (merge ui/global ui/fullscreen ui/column)}
    (div
     {:style (merge ui/row-middle {:padding 8})}
     (comp-tabs by-tabs :key (fn [tab] (println tab)))
     (=< 16 nil)
     (button {:style ui/button, :inner-text "Grab"})
     (=< 16 nil)
     (button {:style ui/button, :inner-text "Set locales"}))
    (div
     {:style (merge ui/expand ui/row)}
     (textarea {:style (merge ui/expand ui/textarea)})
     (textarea {:style (merge ui/expand ui/textarea)}))
    (when dev? (cursor-> :reel comp-reel states reel {})))))
