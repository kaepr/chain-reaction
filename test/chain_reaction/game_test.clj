(ns chain-reaction.game-test
  (:require [chain-reaction.game :as sut]
            [clojure.test :refer [deftest testing is]]))


;; Basic test using is
(deftest utilities-test
  (testing "in-bounds? test valid"
    (is (sut/in-bounds? 0 0))
    (is (sut/in-bounds? 0 5))
    (is (sut/in-bounds? 8 0))
    (is (sut/in-bounds? 8 5)))

  (testing "in-bounds? test invalid"
    (is (not (sut/in-bounds? -1 0)))
    (is (not (sut/in-bounds? 0 6)))
    (is (not (sut/in-bounds? 9 0)))
    (is (not (sut/in-bounds? 9 -1))))

  (testing "corner? test"
    (is (sut/corner? 0 0))
    (is (sut/corner? 0 5))
    (is (sut/corner? 8 0))
    (is (sut/corner? 8 5))
    (is (not (sut/corner? 1 1))))

  (testing "edge? test invalid"
    (is (sut/edge? 0 2))
    (is (sut/edge? 8 0))
    (is (sut/edge? 3 5))
    (is (sut/edge? 0 5))
    (is (not (sut/edge? 1 1)))))
