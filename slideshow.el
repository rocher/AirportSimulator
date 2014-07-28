;
;.sec Buffer Show
;
; Evaluate this buffer to start bufshow as stated below:
;
;.li1 Press or evaluate [[elisp:][(eval-buffer)]] to evaluate this buffer
;
;.li1 Press or evaluate [[elisp:(bufshow-start-presentation)]] to start the buffer show
;.li1 presentation
;
;.li1 To see the next slide, press or evaluate [[elisp:][(bufshow-next)]]
;
;.li1 See previous slide with [[elisp:][(bufshow-prev)]]

(setq bsframe nil)
(bufshow-mode)
(load-library "qwe")

(defun bufshow-start-presentation()
  (interactive)
  (menu-bar-mode 0)
  (if (or (null bsframe)
          (not (framep bsframe))
          (not (select-frame bsframe)))
      (progn
        (setq bsframe (make-frame '((name . "Airport Simulator") (minibuffer . nil))))
        (select-frame bsframe)
        ))
  (bufshow-start
   [("Airplane.java" "Airplane.Size")
    ("Airplane.java" "Airplane.Color")
    ("Airplane.java" "Airplane Members")
    ("Vec3.java" "Vec3")
    ("Airport.pde" "Airport Layout")
    ("Airport.pde" "Collision Detector")
    ("Controller.java" "Check Flight")
    ("Controller.java" "Game Control")
    ])
  )

(defun bufshow-exit()
  (interactive)
  (bufshow-stop)
  (delete-frame bsframe)
  )

;.cfg.footer
;.cfg.mode Local Variables:
;.cfg.mode mode: lisp
;.cfg.mode qwe-delimiter-tag: ""
;.cfg.mode mode: qwe
;.cfg.mode End:
