(module stdlib)

#|
stdlib created by Ivanov Roman and Ivanova Anastasia
|#

(defnat untag r__untag (tagged))

; Binary low level operations are not exposed
(defnat _add r__add (a b))
(defnat _sub r__sub (a b))
(defnat _mul r__mul (a b))
(defnat _div r__div (a b))
(defnat _rem r__rem (a b))

(defnat _gt r__gt (a b))
(defnat _eq r__eq (a b))


(macro _and (a b) `(if a b #f))
(macro _or (a b) `(if a #t b))
(macro not (a) `(if a #f #t))

(defn _lt (a b) (_and
                    (not(_gt a b))
                    (not(_eq a b))
                )
)
(defn _le (a b) (not (_gt a b)
                )
)
(defn _ge (a b) (not (_lt a b)
                )
)


; List related functions
(defnat cons r__withElement (list elem))
(defnat first r__first (list))
(defnat tail r__tail (list))
(defnat size r__size (list))
(defn is-empty (list) (_eq (size list) 0))
(defn list (@args) (args))

; Chain operations
(macro and-list (l)
    (if (is-empty l)
        #t
        `(if (first l)0
            (and-list (tail l))
            #f
        )
     )
)

(macro and (@args) (and-list args))

(macro or-list (args)
     (if (is-empty args)
        #f
        `(if (first args)
            #t
            (or-list (tail args))
        )
     )
)

(macro or (@args) (or-list args))

; input/output
(defnat print r__print (x))
(defn newline () (print "\n"))


;macros section
(macro inc (i) `(_add i 1))

(macro loop (block) `(while #t block))

(macro for (start end body)
    `(let ((i start))
        (while (< i end)
            body
            (inc i)
        )
    )
)


(defn revert (list)
    (if (_eq (size list) 1)
    (cons () (first list))
    (cons (revert (tail list)) (first list)))
)