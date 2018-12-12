(module stdlib)

#|
stdlib created by Ivanov Roman and Ivanova Anastasia
|#

; binary low level operations are not exposed
(defnat _add r__add (a b))
(defnat _sub r__sub (a b))
(defnat _mul r__mul (a b))
(defnat _div r__div (a b))
(defnat _rem r__rem (a b))

(defnat _gt r__gt (a b))
(defnat _eq r__eq (a b))

(defnat ! r__not (b))

(macro _and (a b) (if a b #f))
(macro _or (a b) (if a #t b))
(macro _not (a) (if a #f #t))

(defn _lt (a b) (!(_gt a b) ))
(defn _le (a b) (_or (_lt a b) (_eq a b)))
(defn _ge (a b) (_or (_gt a b) (_eq a b)))


; TODO short circuit and and or

(defnat print r__print (x))

(defnat cons r__withElement (list elem))

(defn newline () (print "\n"))

;macros section

(macro inc (i) `(+ i 1))

(macro loop (block) `(while #t block))

(macro for (start end body)
    `(let ((i start))
        (while (< i end)
            body
            (inc i)
        )
    )
)

(loop (print "1"))

(while #t (print "1"))
