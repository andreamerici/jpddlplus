(define (domain h1_non_if14)
  (:requirements :numeric-fluents :strips)
  (:predicates (has-root) (server-online) (backup-done) (can-compress))
  (:functions (disk-space))

  ;; Azione A: Achiever diretto + Helper
  (:action run-exploit
    :precondition (server-online)
    :effect (and (increase (disk-space) 500) (has-root)))

  ;; Azione B: Achiever diretto che dipende da A
  (:action delete-logs
    :precondition (and (has-root) (backup-done))
    :effect (increase (disk-space) 1000))

  ;; Azione C: Indipendente
  (:action compress-db
    :precondition (can-compress)
    :effect (increase (disk-space) 300))
)