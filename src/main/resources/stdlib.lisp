(module stdlib)

(defnat + r__add (a b))
(defnat - r__sub (a b))
(defnat * r__mul (a b))
(defnat / r__div (a b))

(defnat > r__gt (a b))
(defnat < r__lt (a b))
(defnat = r__eq (a b))

(defnat print r__print (x))

(macro inc (i) ``(+ i 1))

(macro loop (block) ``(while #t block))

(macro for (start end body)
    ``(let ((i start))
        (while (< i end)
            body
            (inc i)
        )
    )
)