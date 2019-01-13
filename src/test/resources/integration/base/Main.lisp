(let ((i 0))
    (while (_lt i 50)
        (print i)
        (print "\n")
        (set i (_add i 1))
    )
    i
)


(defn caller (f) (f "Hello world!"))

(let (
        (x (fn (a) (print a)))
    )
    (caller x)
)