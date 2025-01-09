(ns chain-reaction.ui
  (:require [ring.util.response :as ring-response]
            [rum.core :as rum]
            [cheshire.core :as cheshire]
            [clojure.data.json :as json]
            [ring.middleware.anti-forgery :as csrf]
            [chain-reaction.game :as game]
            [clojure.java.io :as io]))

(defn css-path []
  (if-some [last-modified (some-> (io/resource "public/css/main.css")
                                  ring-response/resource-data
                                  :last-modified
                                  (.getTime))]
    (str "/css/main.css?t=" last-modified)
    "/css/main.css"))

(defn js-path []
  (if-some [last-modified (some-> (io/resource "public/js/main.js")
                                  ring-response/resource-data
                                  :last-modified
                                  (.getTime))]
    (str "/js/main.js?t=" last-modified)
    "/js/main.js"))

(defn static-path [path]
  (if-some [last-modified (some-> (io/resource (str "public" path))
                                  ring-response/resource-data
                                  :last-modified
                                  (.getTime))]
    (str path "?t=" last-modified)
    path))

(def base-settings
  {:base {:title "Chain Reaction"
          :lang "en-US"
          :description "Chain Reaction App"}
   :head [[:link {:rel "stylesheet" :href (static-path "/css/main.css")}]
          [:script {:src (static-path "js/main.js")}]
          [:script {:src "https://unpkg.com/htmx.org@1.9.12"}]
          [:script {:src "https://unpkg.com/htmx.org@1.9.12/dist/ext/ws.js"}]
          [:script {:src "https://unpkg.com/hyperscript.org@0.9.8"}]]})

(defn- base-html [{{:keys [title lang description]} :base
                   head :head} & contents]
  [:html
   {:lang lang
    :style {:min-height "100%"
            :height "auto"}}
   [:head
    [:title title]
    [:meta {:name "description" :content description}]
    (into [:<>] head)]
   [:body
    {:style {:position "absolute"
             :width "100%"
             :min-height "100%"
             :display "flex"
             :flex-direction "column"}}
    contents]])

(defn base [opts & body]
  (apply base-html (merge base-settings opts) body))

(defn page [opts & body]
  (base opts
        [:div
         (when (bound? #'csrf/*anti-forgery-token*)
           {:hx-headers (cheshire/generate-string
                         {:x-csrf-token csrf/*anti-forgery-token*})})
         body]))

(defn home-page []
  (page {}
        [:div {:class "min-h-screen bg-gray-100 flex flex-col items-center justify-center px-4"}
         [:div {:class "max-w-3xl mx-auto px-4 py-16 sm:px-6 lg:px-8 text-center"}
          [:.space-y-12
           [:.space-y-6
            [:h1 {:class "text-4xl md:text-6xl font-bold text-gray-900 tracking-tight"}
             "Chain Reaction"]
            [:p {:class "text:lg md:text-xl text-gray-600 max-w-2xl mx-auto leading-relaxed"}
             "Multiplayer version of "
             [:a {:href "https://brilliant.org/wiki/chain-reaction-game/"
                  :class "inline-flex items-center border-b-2 px-1 text-blue-600 border-blue-300 hover:border-blue-400"}
              "Chain Reaction"]
             "."]
            [:div {:class "flex flex-col sm:flex-row gap-4 justify-center mt-4"}
             [:a {:class "px-8 py-3 text-lg font-semibold rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-200 transition-colors duration-200 cursor-pointer"
                  :href "/sign-in"}
              "Sign In"]
             [:a {:class "px-8 py-3 text-lg font-semibold rounded-lg bg-black text-white hover:bg-gray-700 transition-colors duration-200 cursor-pointer"
                  :href "/sign-up"}
              "Sign Up"]]]]]]))

(defn sign-up-page [sign-up-form]
  (page {}
   [:div {:class "min-h-screen bg-gray-50 flex items-center justify-center p-4"}
    [:div {:class "max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-sm"}
     [:div {:class "text-center"}
      [:h2 {:class "text-3xl font-bold text-gray-900"}
       "Sign Up"]]
     sign-up-form
     [:div {:class "flex justify-center items-center"}
      [:a {:class "cursor-pointer"
           :href "/sign-in"}
       "Go to Sign In"]]]]))

(defn sign-up-form [{:keys [error]}]
  [:form {:class "space-y-6"
             :hx-post "/sign-up"
             :hx-target "this"
             :hx-swap "outerHTML"}
      (when error
        [:div {:class "bg-red-50 border-l-4 border-red-500 p-4 my-4 rounded"}
         [:div {:class "flex items-center text-sm text-red-700"}
          error]])
      [:div
       [:label {:class "block text-sm font-medium text-gray-700"}
        "Username"]
       [:input {:class "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm
                        focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500"
                :name "username"
                :type "text"
                :required true
                :id "username"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700"}
        "Password"]
       [:input {:class "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm
                        focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500"
                :name "password"
                :type "password"
                :required true
                :id "password"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700"}
        "Confirm Password"]
       [:input {:class "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm
                        focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500"
                :name "confirmpassword"
                :type "password"
                :required true
                :id "confirmpassword"}]]
      [:button {:type "submit"
                :class "w-full flex justify-center py-2 px-4 border border-transparent rounded-md
                     shadow-sm text-sm font-medium text-white bg-gray-900 hover:bg-black
                     focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"}
       "Sign Up"]])

(defn sign-in-page [sign-in-form]
  (page {}
   [:div {:class "min-h-screen bg-gray-50 flex items-center justify-center p-4"}
    [:div {:class "max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-sm"}
     [:div {:class "text-center"}
      [:h2 {:class "text-3xl font-bold text-gray-900"}
       "Sign In"]]
     sign-in-form
     [:div {:class "flex justify-center items-center"}
      [:a {:class "cursor-pointer"
           :href "/sign-up"}
       "Go to Sign Up"]]]]))

(defn sign-in-form [{:keys [error]}]
  [:form {:class "space-y-6"
             :hx-post "/sign-in"
             :hx-target "this"
             :hx-swap "outerHTML"}
      (when error
        [:div {:class "bg-red-50 border-l-4 border-red-500 p-4 my-4 rounded"}
         [:div {:class "flex items-center text-sm text-red-700"}
          error]])
      [:div
       [:label {:class "block text-sm font-medium text-gray-700"}
        "Username"]
       [:input {:class "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm
                        focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500"
                :name "username"
                :type "text"
                :required true
                :id "username"}]]
      [:div
       [:label {:class "block text-sm font-medium text-gray-700"}
        "Password"]
       [:input {:class "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm
                        focus:outline-none focus:ring-1 focus:ring-gray-500 focus:border-gray-500"
                :name "password"
                :type "password"
                :required true
                :id "password"}]]
      [:button {:type "submit"
                :class "w-full flex justify-center py-2 px-4 border border-transparent rounded-md
                     shadow-sm text-sm font-medium text-white bg-gray-900 hover:bg-black
                     focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"}
       "Sign In"]])

(defn render-orb [mass color]
  (let [positions {1 [{:cx 12 :cy 12}]
                   2 [{:cx 7 :cy 12} {:cx 17 :cy 12}]
                   3 [{:cx 12 :cy 7} {:cx 7 :cy 17} {:cx 17 :cy 17}]}
        color-class (condp = color
                      :empty "text-gray-300"
                      :red "text-red-500"
                      :green "text-green-500"
                      "text-black-300")]
    [:svg {:viewbox "0 0 24 24"
           :class "w-3/4 h-3/4"}
     (for [pos (get positions mass)
           :let [{:keys [cx cy]} pos]]
       [:circle {:cx cx
                 :cy cy
                 :r "6"
                 :fill "currentColor"
                 :class (str color-class " " "drop-shadow-sm")}
        [:animatetransform
         {:attributename "transform"
          :type "translate"
          :values "-0.3,0.3; 0.3,-0.3; -0.3,0.3"
          :dur "0.6s"
          :repeatcount "indefinite"}]])]))

(defn render-cell [row-idx col-idx cell]
  (let [mass (first cell)
        color (second cell)]
    [:div {:class "border border-gray-300 aspect-square flex items-center justify-center bg-white cursor-pointer transition-all duration-200 hover:bg-gray-100 hover:border-gray-400"
           :hx-vals (json/write-str {:event-type :cell-click
                                     :row row-idx
                                     :col col-idx})
           :ws-send ""
           :name "cell-click"
           :hx-trigger "click"
           :style {:aspect-ratio "1/1"}}
     (render-orb mass color)]))

(defn render-board [board]
  (let []
    [:div {:class "flex justify-center p-6"}
     [:div {:class "w-full max-w-2xl bg-white rounded-lg p-4"}
      [:div {:class "flex mb-1"}
       [:div {:class "w-10"}]
       (for [col-idx (range 0 6)]
         [:div {:class "flex-1 text-center text-lg font-semibold text-gray-700"}
               (str col-idx)])]
      (for [row-idx (range 0 9)]
        [:div {:class "flex"}
         [:div {:class "w-10 flex items-center justify-center text-lg font-semibold text-gray-700"}
          (str row-idx)]
         [:div {:class "flex-1 grid grid-cols-6"}
          (for [col-idx (range 0 6)
                :let [cell (get-in board [row-idx col-idx])]]
            (render-cell row-idx col-idx cell))]])]]))

(defn dashboard [{:keys [username]}]
  (page {}
        [:div {:class "min-h-screen bg-gray-50"}
         ; top bar
         [:div {:class "bg-white shadow"}
          [:div {:class "max-w-5xl mx-auto px-4"}
           [:div {:class "h-16 flex items-center justify-between"}
            [:div {:class "font-medium text-gray-800"}
             username]
            [:button {:class "flex items-center gap-2 text-gray-600 hover:text-gray-800"
                      :hx-post "/logout"}
             "Logout"]]]]
         ; main content
         [:div {:class "max-w-5xl mx-auto px-4 py-8 min-h-screen h-full"}
          [:div {:class "flex flex-col sm:flex-row gap-4 mb-8"}
           [:a {:class "cursor-pointer flex-1 bg-gray-700 text-white px-4 py-2 rounded-lg hover:bg-gray-800 transition-colors"}
            "Create Room"]
           [:a {:class "cursor-pointer flex-1 bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors"}
            "Join Room"]]
          [:div {:class ""
                 :hx-ext "ws"
                 :ws-connect "/room/123"}
           "Websocket connector"
           [:div {:class "border-solid cursor-pointer p-2"
                  :id "div-random-id"
                  :name "cell-click"
                  :ws-send ""
                  :hx-vals (json/write-str {:row 4})
                  :hx-trigger "click"}
            "Click me to send some message"
            (render-board (-> (game/empty-board)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :green 0 0)
                            (game/explode :red 0 0)
                            (game/explode :red 0 0)))]]]]))
          
(defn on-error [{:keys [status ex]}]
  (-> {:status status
       :body (rum/render-static-markup
              (page
               {}
               [:section.bg-gray-200
                [:.container.flex.items-center.min-h-screen.px-6.py-12.mx-auto
                 [:<>
                  [:p.text-xl.font-medium.text-blue-500
                   (if (= status 404)
                     "Not found"
                     "Something went wrong")]]]]))}
   (ring-response/content-type "text/html")))

