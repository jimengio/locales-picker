
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp cursor-> action-> list-> <> div button textarea span input]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [reel.comp.reel :refer [comp-reel]]
            [respo-md.comp.md :refer [comp-md]]
            [app.config :refer [dev?]]
            [respo-alerts.comp.alerts :refer [comp-prompt]]
            [favored-edn.core :refer [write-edn]]
            [cljs.reader :refer [read-string]]
            [clojure.string :as string]))

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
             :on-click (fn [e d! m!] (on-change (:value tab) d! m!))}
            (<> (:display tab)))])))))

(defn pick-locales! [d! locales pick-by draft]
  (let [entries (->> (string/split draft "\n") (filter (comp not string/blank?)))
        entries-set (set entries)]
    (case pick-by
      :key
        (let [chunks (select-keys locales entries)
              missing-keys (->> entries (filter (fn [x] (nil? (get locales x)))))]
          (d! :result chunks)
          (d! :missing missing-keys))
      :zh-cn
        (let [chunks (->> locales
                          (filter (fn [[k info]] (contains? entries-set (get info "zhCN"))))
                          (into {}))
              all-zh (->> locales (map (fn [[k info]] (get info "zhCN"))) (set))
              missing-lang (->> entries (filter (fn [x] (not (contains? all-zh x)))))]
          (d! :result chunks)
          (d! :missing missing-lang))
      (js/console.warn "Unknown kind:" pick-by))))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel)
       states (:states store)
       state (or (:data states) {:draft ""})
       locales (get-in store [:locales :locales])]
   (div
    {:style (merge ui/global ui/fullscreen ui/column)}
    (div
     {:style (merge ui/row-parted {:padding "0 8px"})}
     (div
      {:style (merge ui/row-middle {:padding 8})}
      (comp-tabs by-tabs (:pick-by store) (fn [tab d! m!] (d! :pick-by tab)))
      (=< 16 nil)
      (button
       {:style ui/button,
        :inner-text "Grab",
        :on-click (fn [e d! m!] (pick-locales! d! locales (:pick-by store) (:draft state)))})
      (=< 8 nil)
      (let [locales (get-in store [:locales :locales])]
        (if (some? locales)
          (<> (str (count locales) " keys") {:font-family ui/font-fancy})
          (<> "Empty" {:font-family ui/font-fancy, :color (hsl 0 0 70)})))
      (=< 8 nil)
      (let [missing (:missing store)]
        (if (not (empty? missing))
          (<> (str "Missing: " (string/join ", " (:missing store))) {:color :red}))))
     (cursor->
      :locales
      comp-prompt
      states
      {:trigger (button {:style ui/button, :inner-text "Set locales"}),
       :text "resetting locales",
       :multiline? true,
       :initial (write-edn (:locales store)),
       :input-style {:font-family ui/font-code, :min-width 400, :min-height 320}}
      (fn [result d! m!] (d! :locales (read-string result)))))
    (div
     {:style (merge ui/expand ui/row)}
     (textarea
      {:style (merge ui/expand ui/textarea {:font-family ui/font-code}),
       :value (:draft state),
       :on-input (fn [e d! m!] (m! (assoc state :draft (:value e)))),
       :on-keydown (fn [e d! m!]
         (when (and (= 13 (:key-code e)) (.-metaKey (:event e)))
           (pick-locales! d! locales (:pick-by store) (:draft state))))})
     (textarea
      {:style (merge ui/expand ui/textarea {:font-family ui/font-code}),
       :value (write-edn (:result store)),
       :disabled true}))
    (when true (cursor-> :reel comp-reel states reel {}))
    (when dev? (comp-inspect "data" store {:bottom 0})))))
