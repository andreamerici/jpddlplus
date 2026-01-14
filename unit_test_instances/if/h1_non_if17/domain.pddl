(define (domain h1_non_if17)
  (:requirements :numeric-fluents :strips)
  (:predicates (has-weapon) (boss-key) (in-dungeon))
  (:functions (xp))

  (:action kill-boss
    :parameters ()
    :precondition (and (has-weapon) (in-dungeon))
    :effect (and (increase (xp) 500) (boss-key)))

  (:action open-treasure
    :parameters ()
    :precondition (boss-key)
    :effect (increase (xp) 500))

  (:action grind-minions
    :parameters ()
    :precondition (in-dungeon)
    :effect (increase (xp) 10))
)