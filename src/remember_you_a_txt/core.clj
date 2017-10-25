(ns remember-you-a-txt.core
  (:gen-class)
  (:require [me.raynes.fs :as fs]
            [clojure.string :as s]
            [clj-time.core :as t]))

(def now (t/now))

; so we're gonna need to check for the |> characters that start an expression first - only |>(...)
; but we'll want to narrow it down to just the |> so we can slurp the (...) part
; - we can do that by skipping out on the first two characters of the string 
(def clj-tag "|>(")
(defn clj-tag?
  "string has clj-tag?"
  [st]
  (s/includes? st clj-tag))

; I'll need to get this added as a env variable or config value or something at some point
(def directory (clojure.java.io/file "/Users/sam/Dropbox/notes"))
(def files (file-seq directory))


(defn getPath
  "just a little helper to avoid the java grossness"
  [f]
  (.getPath f))

(defn file->extension
  "turns a java.io.File into it's extension"
  [file]
  (-> file
      getPath
      fs/extension))

(defn str-md-or-txt?
  "is the string given either .md or .txt?"
  [st]
  (or
    (= st ".md")
    (= st ".txt")))
  

(defn file-md-or-txt?
  "is a file a md or txt file (based on extension)"
  [file]
  (-> file
      file->extension
      str-md-or-txt?))

(defn fresh-file
  "make a new file map - with
    :file - the java file object
    :contents - the slurped contents of the file"
  [f]
  {
   :file f
   :contents (slurp f)})

(defn file-with-clj-tag?
  "does a file object contain a reminder?"
  [file]
  (clj-tag? (:contents file)))

(def proper-files (map fresh-file (filter file-md-or-txt? files)))

(def files-with-clj-tags (filter 
                           file-with-clj-tag?
                           proper-files))

(defn index-of-contents
  ([file of]
   (s/index-of (:contents file) of))
  ([file of start]
   (s/index-of (:contents file) of start)))

(defn set-remind-start
  [pf]
  (with-meta pf { :remind-start (index-of-contents pf clj-tag) }))

; so the first step is for us to find where the |> goes, and then read-string on the bit after that...

(defn tag-origin
  "get the offset for the |> tag origin"
  [st]
  (s/index-of st clj-tag))

(defn read-tag ; we'll pretend each file will only have one for now
  "read-string but for after the |>"
  [file]
  (let [contents (:contents file)
        origin (tag-origin contents)]
    (read-string
      (.substring contents (+ 2 origin)))))

(defn remind
  "fill out defaults for any options that arent present, return instructions for execute-for-file to do"
  [opts]
  (let [{:keys [year month day]} opts
        y (if (nil? year)
            (t/year now)
            year)
        m (if (nil? month)
            (t/month now)
            month)
        d (if (nil? day)
            (t/day now)
            day)]
    ; construct a remind-date based on what we have from here. Time is assumed to be 00:01
    {
     :remind-date (t/date-time y m d 00 01)
    }))

; could have the remind file come up with some instructions that could be used for later...
(defn should-remind?
  "take a file, grab the first form that can be executed inside it, and run it with the file in context"
  [file]
  (let [tag (read-tag file)
        instructions (eval tag)]
    (t/before? (:remind-date instructions) now)))

(defn insert-comment
  [content]
  (let [[fst scnd & rst] (s/split content #"\|\>\(")]
    (str fst "|>(comment " scnd (s/join "|>(" rst))))

(defn insert-comment-into-file
  [file]
  {
   :file (:file file)
   :contents (insert-comment (:contents file))})

(defn write-file
  [file])

(let [file (insert-comment-into-file (first files-with-clj-tags))
      writer (clojure.java.io/writer (.getCanonicalPath (:file file)))]
  (.write writer (str (:contents file)))
  (.close writer))

(second (s/split (:contents (first files-with-clj-tags)) #"\|\>\("))

(filter should-remind?  files-with-clj-tags) ; files which need to be reminded...remound?

; stuff for later
; recursion
; more flexible reminders 'monday next week'
; checking for multiple things
; more operations you can apply to the file...


