(define (domain d_xyz)
  (:requirements :typing :numeric-fluents)
  (:predicates
    (p_x)
    (p_y)
    (p_z)
    (p_w)
    (p_goal)
  )
  (:functions
    (f_a) - number
    (f_b) - number
  )

  (:action a_1
    :parameters ()
    :effect (p_x)
  )

  (:action a_2
    :parameters ()
    :precondition (p_x)
    :effect (p_y)
  )

  (:action a_3
    :parameters ()
    :precondition (and (p_x) (p_y))
    :effect (p_z)
  )

  (:action a_slow
    :parameters ()
    :precondition (p_x)
    :effect (increase (f_a) 1)
  )

  (:action a_fast
    :parameters ()
    :precondition (p_z)
    :effect (increase (f_a) 50)
  )

  (:action a_end
    :parameters ()
    :precondition (and (>= (f_a) 500))
    :effect (p_goal)
  )
)