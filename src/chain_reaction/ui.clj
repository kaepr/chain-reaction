(ns chain-reaction.ui
  (:require [ring.util.response :as ring-response]
            [rum.core :as rum]
            [cheshire.core :as cheshire]
            [ring.middleware.anti-forgery :as csrf]
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
         [:div {:class "max-w-5xl mx-auto px-4 py-8"}
          [:div {:class "flex flex-col sm:flex-row gap-4 mb-8"}
           [:a {:class "flex-1 bg-gray-700 text-white px-4 py-2 rounded-lg hover:bg-gray-800 transition-colors"}
            "Create Room"]
           [:a {:class "flex-1 bg-gray-600 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors"}
            "Join Room"]]]]))

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

