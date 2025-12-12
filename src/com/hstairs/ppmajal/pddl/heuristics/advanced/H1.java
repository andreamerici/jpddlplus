/*
 * Copyright (C) 2010-2017 Enrico Scala. Contact: enricos83@gmail.com.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hstairs.ppmajal.pddl.heuristics.advanced;

import com.google.common.collect.Sets;
import com.hstairs.ppmajal.conditions.*;
import com.hstairs.ppmajal.expressions.ExtendedAddendum;
import com.hstairs.ppmajal.expressions.ExtendedNormExpression;
import com.hstairs.ppmajal.expressions.NumEffect;
import com.hstairs.ppmajal.expressions.NumFluent;
import com.hstairs.ppmajal.extraUtils.ArrayShifter;
import com.hstairs.ppmajal.PDDLProblem.PDDLProblem;
import com.hstairs.ppmajal.problem.State;
import com.hstairs.ppmajal.search.SearchHeuristic;
import com.hstairs.ppmajal.transition.Transition;
import static com.hstairs.ppmajal.transition.Transition.getTransition;
import com.hstairs.ppmajal.transition.TransitionGround;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import static java.lang.Math.ceil;
import java.util.*;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.Map.Entry;
/**
 * Implementazione di una euristica in stile h_add/h_max su un grafo rilassato.
 *
 * Idee chiave:
 * - Rappresentazione compatta del problema (cp = CompactPDDLProblem) per accedere
 *   rapidamente a precondizioni/effetti/costi delle "azioni compatte".
 * - Si mantengono costi stimati per azioni e condizioni terminali e si propaga
 *   la miglior stima con una coda a priorità (Fibonacci heap).
 * - Supporto sia proposizionale sia numerico: per i confronti numerici si stima
 *   il numero di ripetizioni di un'azione necessarie a soddisfare la disuguaglianza.
 * - Opzionalmente si estrae un piano rilassato (relaxed plan) e le azioni "helpful".
 *
 * Parametri principali:
 * - additive: se true si comporta come h_add, altrimenti usa max sui sotto-obiettivi.
 * - conjunctionsMax: forza la combinazione a massimo nelle congiunzioni.
 * - extractRelaxedPlan/helpful*: abilita estrazione MRP e calcolo azioni utili.
 *
 *
 * @author enrico
 */
public class H1 implements SearchHeuristic {

    /**
     * Flag di debug per stampe interne.
     */
    static final boolean DEBUG = false;

    // Opzioni per relax-plan e helpful transitions
    final public boolean extractRelaxedPlan;
    final public boolean maxMRP;

    boolean isDomainInterferenceFree;

    // Problema compatto su cui si lavora
    public final CompactPDDLProblem cp;

    // Numero totale di termini/condizioni (ID di Terminal)
    protected final int totNumberOfTerms;
    protected final int totNumberOfTermsRefactored;

    // Problema originale e opzioni varie
    protected final PDDLProblem problem;
    final private boolean helpfulActionsComputation;

    // Strutture di supporto per aggiornamenti incrementali
    final IntArraySet[] conditionsAchievableBy;   // cache: condizioni raggiungibili da azione
    final IntArraySet[] conditionsDeletableBy;    // opzionale: condizioni peggiorabili da azione (smart constraints)
    final IntArraySet[] conditionToAction;        // mappa condizione -> azioni che la richiedono
    final IntArraySet allConditions;              // insieme di condizioni terminali usate nel grafo
    private final IntArraySet allComparisons;     // sottoinsieme di condizioni numeriche (Comparison)

    // Contiene gli id dei terminali per la precondizione di ogni azione.
    final IntArraySet[] actionPreconditionTerminals;

    // Nodo associato all'azione nella coda di Fibonacci
    protected final FibonacciHeapNode[] nodeOf;

    // Opzioni di raggiungibilità: se attive, si accumulano le azioni viste
    boolean reachability;
    private final boolean conjunctionsMax;

    // Vettori di costo: per azioni (costo dei prerequisiti) e per condizioni
    final float[] actionHCost;              // costo η(a) per rendere precond. vere
    private final float[] conditionCost;    // costo stimato ω(ψ)

    // Azioni "chiuse" (espanse) nella Dijkstra/Uniform-Cost like
    protected final boolean[] closed;

    // Se true => h_add (somma), se false => h_max/misto
    final boolean additive;

    // Condizioni/azioni vere a costo 0 nello stato iniziale
    private final boolean[] conditionInit;
    private final boolean[] actionInit;

    // Se aggiungere suggerimenti di transizioni helpful nell'insieme restituito
    private final boolean helpfulTransitions;

    // Gestione memoria per contributi numerici
    private final boolean hardcoreVersion;
    private final float[][] numericContributionRaw;
    private final Map<Pair<Integer, Integer>, Float> numericContribution; // alternativa a mappa

    // Rifasatori per indicizzare compatto (riducono spazio quando gli ID sono sparsi)
    protected final ArrayShifter termsArrayShifter;
    protected final ArrayShifter actionsArrayShifter;
    protected final int totNumberOfActionsRefactored;

    // Achievers ed info per rilassato/smart constraints
    IntArraySet[] allAchievers;
    final private IntArraySet[] deleters;
    protected int[] establishedAchiever;
    private IntArraySet[] indirectAchievers;
    protected float[] numRepetition;
    private List helpfulActions;
    IntArraySet reachableTransitions;
    private Collection<TransitionGround> reachableTransitionsInstances;

    // Costante speciale: effetto numerico sconosciuto/complesso
    final float UNKNOWNEFFECT = Float.NEGATIVE_INFINITY;

    // Azioni con precondizioni vuote attive all'inizio a costo 0
    final protected IntArraySet freePreconditionActions;

    // Insieme (di ID) del piano rilassato
    private IntArraySet plan;

    // Per ogni transizione reale, insieme di ripetizioni necessarie osservate nel piano
    final protected IntArraySet[] repetitionsInThePlan;

    // Per strategia non additiva: minimo costo dei prerequisiti fra gli achiever
    private float[] minAchieverPreconditionCost;    // costo min achiever per cond. numeriche

    // Insieme di tutte le azioni compatte considerate dall’euristica
    protected IntArraySet allActions;

    // Abilitazione di vincoli smart
    final boolean useSmartConstraints;

    // Strutture per visita nel relaxed plan
    final boolean[] visited;
    protected final int[] maxNumRepetition ;
    public boolean[] helpfulTransitionsMap = null;
    private boolean hardConditionthroughNumError;
    private Collection<TransitionGround> initActions;
    private final boolean storeInitActions;

    private boolean isHelpfulMap = false;

    public H1(PDDLProblem problem) {
        this(problem, true, false, false, "no", false, false, false, false, null, false, -1);
    }



    public H1(PDDLProblem problem, boolean additive) {
        this(problem, additive, false, false, "no", false, false, false, false, null, false, -1);
    }

    /**
     * Costruttore "completo", consente di configurare tutte le modalità della H1.
     * Vedere altri costruttori per default sensati.
     */
    public H1(PDDLProblem problem, boolean additive, boolean extractRelaxedPlan, boolean maxHelpfulTransitions, String redConstraints, boolean helpfulActionsComputation, boolean reachability,
              boolean helpfulTransitions, boolean conjunctionsMax, boolean unitaryCost, int linearEffectsAbstraction) {
        this(problem, additive, extractRelaxedPlan, maxHelpfulTransitions,
                redConstraints, helpfulActionsComputation, reachability,
                helpfulTransitions, conjunctionsMax, null, unitaryCost, linearEffectsAbstraction);
    }

    public H1(PDDLProblem problem, boolean additive, boolean extractRelaxedPlan, boolean maxHelpfulTransitions, String redConstraints, boolean helpfulActionsComputation, boolean reachability,
              boolean helpfulTransitions, boolean conjunctionsMax, boolean unitaryCost) {
        this(problem, additive, extractRelaxedPlan, maxHelpfulTransitions,
                redConstraints, helpfulActionsComputation, reachability, helpfulTransitions,
                conjunctionsMax, null, unitaryCost, -1);
    }

    /**
     * Costruisce la struttura euristica:
     * - Trasforma il problema in rappresentazione compatta (cp).
     * - Inizializza insiemi/array per costi e caching.
     * - Pre-registra le precondizioni per tutte le azioni compatte e il goal.
     */
    public H1(PDDLProblem problem, boolean additive, boolean extractRelaxedPlan, boolean maxHelpfulTransitions, String redConstraints, boolean helpfulActionsComputation, boolean reachability,
              boolean helpfulTransitions, boolean conjunctionsMax, Map<AndCond,
                    Collection<IntArraySet>> redundantMap, boolean unitaryCost, int compNumericStrategy) {
        this.storeInitActions = false;
        long startSetup = System.currentTimeMillis();
        this.additive = additive;
        this.problem = problem;
        this.reachability = reachability;
        this.helpfulActionsComputation = helpfulActionsComputation;
        this.extractRelaxedPlan = extractRelaxedPlan;
        allComparisons = new IntArraySet();
        freePreconditionActions = new IntArraySet();
        hardConditionthroughNumError = compNumericStrategy > -2;
        // Se abilitata, stampa che le condizioni numeriche hard verranno gestite con penalità
        if (hardConditionthroughNumError)
            System.out.println("Numeric Error for Complex Condition Activated");

        // Rappresentazione compatta del problema (pre, effetti, costi, mapping)
        cp = ProblemTransfomer.generateCompactProblem(problem, redConstraints, unitaryCost, compNumericStrategy);
        useSmartConstraints = "smart".equals(redConstraints);

        // Spazio di condizioni/termini e strutture di indicizzazione
        totNumberOfTerms = Terminal.getTotCounter();
        conditionsAchievableBy = new IntArraySet[cp.numActions()];
        conditionToAction = new IntArraySet[totNumberOfTerms];
        allConditions = new IntArraySet();
        allActions = new IntArraySet();

        // Alloca nodi per heap e popola mapping precondizioni/azioni
        nodeOf = new FibonacciHeapNode[cp.numActions()];

        actionPreconditionTerminals = new IntArraySet[cp.numActions()];

        fillPreEffFunctions(new LinkedHashSet(problem.actions));
        fillPreEffFunctions(new LinkedHashSet(problem.getEventsSet()));
        fillPreEffFunctions(new LinkedHashSet(problem.getProcessesSet()));

        // Aggiunge la pseudo-azione di goal e registra le sue precondizioni
        allActions.add(cp.goal());
        updatePreconditionFunction(cp.goal());

        // Rifasatori per indicizzazione compatta in matrici ??
        termsArrayShifter = new ArrayShifter(getAllConditions());
        totNumberOfTermsRefactored = termsArrayShifter.getMaxTid();

        actionsArrayShifter = new ArrayShifter(allActions);
        totNumberOfActionsRefactored = actionsArrayShifter.getMaxTid();

        // Vettori di costo per la ricerca best-first/Uniform-Cost-like
        actionHCost = new float[cp.numActions()];
        conditionCost = new float[totNumberOfTerms];
        closed = new boolean[cp.numActions()];

        // Sceglie rappresentazione del contributo numerico in base alla memoria prevista
        hardcoreVersion = cp.numActions() * totNumberOfTermsRefactored < 1999999999;
        if (hardcoreVersion) {
            numericContributionRaw = new float[totNumberOfActionsRefactored][totNumberOfTermsRefactored];
            for (final float[] row : numericContributionRaw) {
                Arrays.fill(row, Float.MAX_VALUE);
            }
            numericContribution = null;
        } else {
            // Versione con mappa (minore footprint immediato per sparse)
            numericContributionRaw = null;
            numericContribution = new HashMap<>();
        }

        // Inizializza vettori di stato di partenza
        conditionInit = new boolean[totNumberOfTerms];
        actionInit = new boolean[cp.numActions()];

        // Se servono achievers/deleters li alloca ora
        if (extractRelaxedPlan || useSmartConstraints || helpfulActionsComputation) {
            allAchievers = new IntArraySet[totNumberOfTerms];
        }
        if (useSmartConstraints) {
            deleters = new IntArraySet[totNumberOfTerms];
            conditionsDeletableBy = new IntArraySet[cp.numActions()];
        } else {
            deleters = null;
            conditionsDeletableBy = null;
        }
        if (extractRelaxedPlan || helpfulActionsComputation) {
            establishedAchiever = new int[totNumberOfTerms];
            numRepetition = new float[totNumberOfTerms];
        }
        this.helpfulTransitions = helpfulTransitions;
        if (!additive) {
            // Per la combinazione "max" si traccia il minimo costo di prerequisiti
            minAchieverPreconditionCost = new float[totNumberOfTerms];
        }

        maxMRP = maxHelpfulTransitions;
        this.conjunctionsMax = conjunctionsMax;
        System.out.println("H1 Setup Time (msec): " + (System.currentTimeMillis() - startSetup));

        // Strutture per relaxed plan e helpful actions
        if (extractRelaxedPlan || helpfulActionsComputation){
            maxNumRepetition = new int[Transition.totNumberOfTransitions+1];
            visited = new boolean[totNumberOfTerms];
            repetitionsInThePlan = new IntArraySet[Transition.totNumberOfTransitions+1];
        } else {
            visited = null;
            maxNumRepetition = null;
            repetitionsInThePlan = null;
        }
        computeInterferenceFree();
    }

    /**
     * Registra tutte le azioni derivate da una transizione
     * e ne indicizza le precondizioni terminali per aggiornamenti veloci.
     */
    private void fillPreEffFunctions(LinkedHashSet<TransitionGround> transitions) {

        for (final TransitionGround b : transitions) {
            for (final int i : cp.tr2CpTrMap()[b.getId()]){
                allActions.add(i);
                updatePreconditionFunction(i);
            }
        }

    }


    /**
     * Estrae le condizioni terminali dalla precondizione dell'azione compatta i
     * e popola:
     * - freePreconditionActions se non ci sono terminali (azione attivabile gratis);
     * - conditionToAction per notificare quali azioni dipendono da quale condizione;
     * - allConditions e allComparisons
     */
    void updatePreconditionFunction(int i) {
        // estrae tutte le condizioni terminali dalla precondizione.
        final Collection<Condition> terminalConditions = cp.preconditionFunction()[i].getTerminalConditionsInArray();

        // Inizializza il set per questa azione (anche se le precondizioni sono vuote)
        actionPreconditionTerminals[i] = new IntArraySet();

        if (terminalConditions.isEmpty()) {
            freePreconditionActions.add(i);
        }

        // Itera su ogni terminale trovato
        for (final Condition c : terminalConditions) {
            if (c instanceof Terminal) {
                if (c instanceof Comparison) {
                    final Comparison normalize = (Comparison) c.normalize();
                    final int nid = normalize.getId();
                    actionPreconditionTerminals[i].add(nid);
                    IntArraySet groundActions = getConditionToAction()[nid];
                    if (groundActions == null) {
                        groundActions = new IntArraySet();
                    }
                    groundActions.add(i);
                    conditionToAction[nid] = groundActions;
                    getAllConditions().add(nid);
                    getAllComparisons().add(nid);
                } else {
                    final Terminal t = (Terminal) c;
                    final int tid = t.getId();
                    actionPreconditionTerminals[i].add(tid);
                    IntArraySet groundActions = getConditionToAction()[tid];
                    if (groundActions == null) {
                        groundActions = new IntArraySet();
                    }
                    groundActions.add(i);
                    conditionToAction[tid] = groundActions;
                    getAllConditions().add(tid);
                }
            }
        }
    }

    /**
     * Prepara lo stato iniziale della propagazione:
     * - Reset di costi e flag;
     * - Inserimento in coda delle azioni con precondizioni inizialmente soddisfatte
     *   (o freePreconditionActions) con costo 0;
     * - Propagazione iniziale delle condizioni vere nello stato di input.
     * Il metodo smallSetup(State gs) inizializza tutti i costi a Float.MAX_VALUE,
     * segna i literal veri nello stato iniziale impostando conditionCost[t]=0
     * per i terminal veri e chiama updateActions sulle condizioni vere
     * per inserire azioni inizialmente applicabili nella coda con costo 0.
     * Questo corrisponde all’inizializzazione di ω e di η.
     */
    protected FibonacciHeap smallSetup(State gs) {
        /*  actionHCost = costo corrente stimato per ciascuna azione nell’open-list (inizialmente infinito).
            conditionCost = costo stimato per ciascuna condizione (terminal literal), inizialmente infinito.
            closed = flag che indica se una azione è stata già "espansa"/chiusa.
            actionInit / conditionInit = flag che indicano se quell’elemento è stato inizializzato (messo nella coda o segnato come raggiunto dallo starting state).
         */
        Arrays.fill(getActionHCost(), Float.MAX_VALUE);
        Arrays.fill(getConditionCost(), Float.MAX_VALUE);
        Arrays.fill(getClosed(), false);
        Arrays.fill(getActionInit(), false);
        Arrays.fill(getConditionInit(), false);

        /*
        establishedAchiever e numRepetition sono strutture usate per la ricostruzione del relaxed plan
        (e per criteri di ripetizione/numero di volte che un achiever è considerato).
        Vengono impostate solo quando serve la ricostruzione del piano rilassato o il calcolo delle helpful actions.
         */
        if (extractRelaxedPlan || isHelpfulActionsComputation()) {
            Arrays.fill(establishedAchiever, -1);
            Arrays.fill(numRepetition, Float.MAX_VALUE);
        }

        /*
        In domini non-additivi serve tracciare ulteriori informazioni sulla distribuzione dei costi dei precondizioni per trovare il min achiever corretto.
         */
        if (!isAdditive()) {
            Arrays.fill(minAchieverPreconditionCost, Float.POSITIVE_INFINITY);
        }

        final FibonacciHeap h = new FibonacciHeap();

        // Condizioni già vere nello stato hanno costo 0: notifica le azioni che le richiedono
        for (final int i : getAllConditions()) {
            if (gs.satisfy(Terminal.getTerminal(i))) {
                conditionCost[i] = 0f;
                conditionInit[i] = true;
                updateActions(i, h, true);
            }
        }

        // Azioni con precondizioni vuote attive all'inizio a costo 0
        for (final int freePreconditionAction : freePreconditionActions) {
            actionHCost[freePreconditionAction] = 0f;
            actionInit[freePreconditionAction] = true;
            addActionsInPriority(freePreconditionAction, h, 0f);
        }

        // Opzionale: salva l'elenco delle azioni iniziali (se abilitato)
        if (storeInitActions){
            initActions = new ArrayList<>();
            for (var act: allActions ){
                try {
                    initActions.add((TransitionGround) getTransition(cp.cpTr2TrMap()[act]));
                }catch(final Exception e){
                    throw new UnsupportedOperationException("Init actions storage only works senza processi attivi");
                }
            }
        }
        return h;
    }

    /**
     * Esegue la propagazione dei costi sul grafo rilassato.
     */
    @Override
    public float computeEstimate(State gs) {
        final FibonacciHeap h = this.smallSetup(gs);
        // dontstop true => calcolo anche insiemi di raggiungibilità
        final boolean dontstop = reachability || reachableTransitions == null;

        while (!h.isEmpty()) {
            // Si estrae l’azione con costo minimo
            final int actionId = (int) h.removeMin().getData();

            if (actionId == cp.goal() && !dontstop) {
                break;
            }

            // Se dontstop è attivo e l’azione non è il goal, l’azione viene aggiunta a reachableTransitions.
            if (dontstop && actionId != cp.goal()) {
                if (reachableTransitions == null) {
                    reachableTransitions = new IntArraySet();
                }
                reachableTransitions.add(actionId);
            }

            // Si marca closed[actionId] = true.
            closed[actionId] = true;

            // Se actionId != cp.goal() allora si chiama expand(actionId, h, gs),
            // che aggiorna i costi delle condizioni che l’azione può rendere vere (e quindi invoca updateActions per quelle condizioni)
            if (actionId != cp.goal()) {
                expand(actionId, h, gs);
            }
        }

        // Se, al termine del ciclo, il goal non è stato raggiunto euristicamente
        if (getActionHCost()[cp.goal()] == Float.MAX_VALUE ){
            return Float.MAX_VALUE;
        }

        // Estrazione del piano rilassato (se richiesto)
        if (this.extractRelaxedPlan){
            return relaxedPlanCost(gs);
        }

        // Calcolo h_add (o h_max) con ricostruzione helpful (se richiesto)
        if (this.isHelpfulActionsComputation()){
            relaxedPlanCost(gs);
        }
        return getActionHCost()[cp.goal()];

    }

    /**
     * Inserisce un'azione nella coda con priorità v, memorizzando il nodo.
     */
    void addActionsInPriority(final int i, final FibonacciHeap p, final float v) {
        final FibonacciHeapNode fibonacciHeapNode = new FibonacciHeapNode(i);
        nodeOf[i] = fibonacciHeapNode;
        p.insert(fibonacciHeapNode, v);
    }

    protected void updateActions(final int c, final FibonacciHeap p) {
        this.updateActions(c, p, false);
    }

    /**
     * Dato un ID di condizione c diventata più economica, ricalcola la stima
     * dei prerequisiti per tutte le azioni che dipendono da c e aggiorna la coda.
     * Se init==true e il costo precondizioni è 0, marca l’azione come "init".
     *
     * Funzione che notifica tutte le azioni che hanno i tra le loro precondizioni:
     * aggiorna costi parziali delle azioni e può inserire azioni nella coda quando
     * tutte/sufficienti precondizioni sono state raggiunte.
     */
    protected void updateActions(final int c, final FibonacciHeap p, boolean init) {
        // trovare tutte le azioni (actionId) che utilizzano la condizione come precondizione terminale.
        final IntArraySet actions = getConditionToAction()[c]; //getConditionToAction() è un array in cui ogni indice (conditionId) punta a un insieme (IntSet) di ID di azioni che hanno quella condizione tra i loro prerequisiti.

        if (actions != null) {

            // Il metodo itera su tutte le azioni contenute in actions.
            // Per ciascuna azione, esegue il ricalcolo del suo costo euristico
            for (final int i : actions) {
                if (!getClosed()[i]) { // Se l'azione non è ancora chiusa
                    // Calcolo di η_nuovo(a)
                    float v = estimateCost(cp.preconditionFunction()[i], getActionHCost()[i]);

                    if (init && v == 0) {
                        actionInit[i] = true;
                    }

                    // Aggiornamento e Gestione dell'Heap
                    if (v < Float.MAX_VALUE) {
                        if (v < getActionHCost()[i]) {
                            if (getActionHCost()[i] == Float.MAX_VALUE) {
                                actionHCost[i] = v;
                                addActionsInPriority(i, p, v);
                            } else {
                                actionHCost[i] = v;
                                p.decreaseKey(getNodeOf()[i], v);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Ricostruisce il piano rilassato a partire dalla precondizione del goal.
     * Si navigano ricorsivamente le condizioni attivate/achievers stabiliti
     * e si accumulano le ripetizioni necessarie per ciascuna azione.
     */
    protected float relaxedPlanCost(State gs) {
        final Condition goal = cp.preconditionFunction()[cp.goal()];

        final LinkedList<Pair<Collection, Float>> stack = new LinkedList();
        stack.push(getActivatingConditions(goal));
        plan = new IntArraySet();
        Arrays.fill(visited, false);
        helpfulActions = new ArrayList();
        Arrays.fill(maxNumRepetition, 0);
        Arrays.fill(repetitionsInThePlan, null);

        if (isHelpfulMap){
            helpfulTransitionsMap = new boolean[Transition.totNumberOfTransitions];
        }
        while (!stack.isEmpty()) {

            final Pair<Collection, Float> elements;
            elements = stack.pollLast();
            for (final int conditionId : (Collection<Integer>) elements.getFirst()) {
                if (!visited[conditionId]) {
                    if (!getConditionInit()[conditionId]) {
                        // Helpful actions: tra gli achiever di questa condizione, quelli già "init"
                        if (isHelpfulActionsComputation()) {
                            if (getAchievers(conditionId).isEmpty()) {
                                throw new RuntimeException("Houston we have problem here. Condition \n" + Terminal.getTerminal(conditionId) + " has never been achieved");
                            }
                            for (final int id : getAchievers(conditionId)) {
                                if (getActionInit()[id]) {

                                    helpfulActions.add((TransitionGround) getTransition(cp.cpTr2TrMap()[id]));
                                    if (helpfulTransitionsMap != null){
                                        helpfulTransitionsMap[cp.cpTr2TrMap()[id]] = true;
                                    }
                                }
                            }
                        }

                        final int actionId = establishedAchiever[conditionId];
                        final int rep = (int) ceil(numRepetition[conditionId]);
                        final int trActionId = cp.cpTr2TrMap()[actionId];
                        if (repetitionsInThePlan[trActionId] == null){
                            repetitionsInThePlan[trActionId] = new IntArraySet();
                        }

                        // Traccia le ripetizioni osservate per l’azione reale
                        if (maxNumRepetition[trActionId] != rep){
                            repetitionsInThePlan[trActionId].add(rep);
                            maxNumRepetition[trActionId] = Math.max(maxNumRepetition[trActionId],rep);
                        }
                        // Aggiunge l’azione reale al piano rilassato
                        plan.add(cp.cpTr2TrMap()[actionId]);
                        // Espande ricorsivamente le precondizioni dell’azione scelta
                        stack.push(getActivatingConditions(cp.preconditionFunction()[actionId]));
                    }
                    visited[conditionId] = true;
                }
            }
        }

        // Costo del piano in funzione della politica maxMRP
        float ret = 0;
        for (final int action : plan) {
            // tutte le azioni compatte per una data transizione condividono il costo
            final var t = cp.tr2CpTrMap()[action].iterator().next();
            ret += maxNumRepetition[action] * getActionCost()[t];
        }
        return ret;
    }

    @Override
    public Collection getAllEstimates() {
        return SearchHeuristic.super.getAllEstimates(); // default
    }

    /**
     * Restituisce (e crea se necessario) l'insieme di achiever per una condizione.
     */
    public IntArraySet getAchievers(int conditionId) {
        final IntArraySet achiever = getAllAchievers()[conditionId];
        if (achiever == null) {
            getAllAchievers()[conditionId] = new IntArraySet();
        }
        return getAllAchievers()[conditionId];
    }

    /**
     * Espansione di un'azione estratta dalla coda:
     * - Per ogni condizione che l’azione può rendere vera (proposizionale o numerica),
     * calcola il nuovo costo candidato e, se migliora, aggiorna la stima e il relax plan.
     */
    private void expand(int actionId, FibonacciHeap p, State s) {

        // Recupera tutte le condizioni terminali che l'azione può potenzialmente rendere vere o migliorare.
        final IntSet conditionsAchievableByAction = getConditionsAchievableById(actionId);

        for (final int conditionId : conditionsAchievableByAction) { // tutte le condizioni terminali influenzate

            // La condizione !getConditionInit()[conditionId] assicura che si considerino solo le condizioni non ancora soddisfatte nello stato iniziale.
            if (!getConditionInit()[conditionId] && (!isReachability() || getConditionCost()[conditionId] == Float.MAX_VALUE)) {
                final Terminal t = Terminal.getTerminal(conditionId);
                boolean update = false;

                if (t instanceof BoolPredicate || t instanceof NotCond) { // effetto proposizionale

                    // Nuovo Costo: costo dei prerequisiti (getActionHCost()[actionId]) + costo dell'azione stessa (getActionCost()[actionId]).
                    // Se il nuovo costo è minore del costo corrente (updateIfNeeded), la condizione viene aggiornata e l'azione viene registrata come achiever con 1 ripetizione.
                    if (updateIfNeeded(conditionId, getActionHCost()[actionId] + getActionCost()[actionId])) {
                        update = true;
                        cacheValue(getActionCost()[actionId],actionId,t);
                        updateRelPlanInfo(conditionId, actionId, 1);
                    }
                } else { // confronto numerico

                    // Viene calcolato quanto l'azione influenza la variabile numerica coinvolta
                    final double v = this.numericContribution(actionId, (Comparison) t);

                    if (v > 0) {
                        // Quante volte ripetere l’azione per soddisfare la disuguaglianza
                        float rep = computeRepetition(t,v,s);
                        final float executionCost = rep * getActionCost()[actionId]; // Costo di esecuzione rep * gamma(a)
                        boolean localUpdate = false;

                        if (isAdditive() || this.isDomainInterferenceFree) {
                            // Se h_add O h_max Interference-Free:
                            // Costo = CostoPrerequisiti(a) + CostoEsecuzione(a) (minimo della somma)
                            // Il costo cumulato è la somma del costo dei prerequisiti e del costo di esecuzione (executionCost).
                            localUpdate = updateIfNeeded(conditionId, getActionHCost()[actionId] + executionCost);
                        } else {
                            // Logica h_max standard
                            // Costo = minAchieverPreconditionCost + CostoEsecuzione(a) (somma dei minimi)
                            // Si usa la somma del minimo costo dei prerequisiti tra tutti gli achievers visti finora e il costo di esecuzione.
                            if (getActionHCost()[actionId] < minAchieverPreconditionCost[conditionId]) {
                                minAchieverPreconditionCost[conditionId] = getActionHCost()[actionId];
                            }
                            localUpdate = updateIfNeeded(conditionId, minAchieverPreconditionCost[conditionId] + executionCost);
                        }
                        if (localUpdate) {
                            cacheValue(executionCost,actionId,t);
                            update = true;
                            updateRelPlanInfo(conditionId, actionId, rep);
                        }
                    } else if (v == UNKNOWNEFFECT) { // effetto difficile/non lineare: gestione conservativa
                        float newCost = 0f;
                        float rep = computeRepetition(t, 1f, s);
                        if (rep < 0){
                            rep = 0f;
                        }
                        if (isAdditive()) {
                            // Se attivato, penalizza in proporzione a quante volte ipotizziamo di ripetere l'azione
                            if (hardConditionthroughNumError) {
                                newCost = rep*getActionCost()[actionId];
                            } else {
                                newCost = getActionCost()[actionId];
                            }
                        }
                        if (updateIfNeeded(conditionId, getActionHCost()[actionId] + newCost)) {
                            update = true;
                            updateRelPlanInfo(conditionId, actionId, rep);
                        }
                    }
                }
                // Se un qualsiasi aggiornamento è avvenuto (update = true), il nuovo costo della condizione viene propagato alle azioni che la richiedono:
                if (update) {
                    updateActions(conditionId, p);
                }
            }
        }

    }

    /**
     * Memorizza l’azione come achiever (se necessario per relaxed plan/smart constraints).
     */
    protected void updateAchievers(int conditionId, int actionId) {
        // necessario anche per il controllo Interference-Free: popola sempre gli achievers
        getAchievers(conditionId).add(actionId);
    }

    /**
     * Aggiorna le strutture per la ricostruzione del piano rilassato.
     */
    protected void updateRelPlanInfo(int conditionId, int actionId, float rep) {
        if (extractRelaxedPlan || isHelpfulActionsComputation()) {
            establishedAchiever[conditionId] = actionId;
            numRepetition[conditionId] = rep;
        }
    }

    /**
     * Aggiorna il costo di una condizione se è migliorativo.
     */
    protected boolean updateIfNeeded(final int t, final float value) {
        if (getConditionCost()[t] > value) {
            conditionCost[t] = value;
            return true;
        }
        return false;
    }

    /**
     * Restituisce le condizioni terminali "attivanti" per una formula
     * e il costo cumulato corrispondente
     */
    protected Pair<Collection, Float> getActivatingConditions(final Condition c) {
        if (c instanceof AndCond) {
            final AndCond and = (AndCond) c;
            if (and.sons == null || and.sons.length == 0) {
                return Pair.of(Collections.EMPTY_LIST, 0f);
            }
            IntArraySet left = new IntArraySet();
            float cost = 0f;
            for (final var son :  and.sons) {
                Pair<Collection, Float> activatingConditions = getActivatingConditions((Condition) son);
                cost += activatingConditions.getSecond();
                left.addAll(activatingConditions.getFirst());
            }
            return Pair.of(left, cost);

        } else if (c instanceof OrCond) {
            final OrCond or = (OrCond) c;
            if (or.sons == null || or.sons.length==0) {
                return Pair.of(Collections.EMPTY_LIST, 0f);
            }
            float ret = Float.MAX_VALUE;
            Collection left = null;
            for (final var son :  or.sons) {
                final Pair<Collection, Float> estimate = getActivatingConditions((Condition) son);
                if (estimate.getSecond() != Float.MAX_VALUE) {
                    if (estimate.getSecond() < ret) {
                        ret = estimate.getSecond();
                        left = estimate.getFirst();
                    }
                }
            }
            return Pair.of(left, ret);
        } else if (c instanceof Terminal) {
            final Terminal t = (Terminal) c;
            return Pair.of(new IntArraySet(Collections.singleton(t.getId())), getConditionCost()[t.getId()]);
        } else {
            throw new RuntimeException("This is not supported:" + c);
        }
    }

    @Override
    public boolean[] getHelpfulTransitionMap() {
        return this.helpfulTransitionsMap;
    }

    /**
     * Stima il costo per soddisfare una formula c
     */
    protected float estimateCost(final Condition c, float previous) {
        return this.estimateCost(c, isAdditive(),previous);
    }


    private float estimateCost(final Condition c, boolean additive, float previous) {
        if (c instanceof AndCond and) {
            if (and.sons == null) {
                return 0f;
            }
            float ret = 0f;
            for (final var son : and.sons) {
                final float estimate = estimateCost((Condition) son,previous);
                if (estimate == Float.MAX_VALUE || estimate >=previous) {
                    return estimate;
                }
                if (additive && !isConjunctionsMax()) {// h_add
                    ret += estimate;
                } else { // h_max
                    ret = Math.max(estimate, ret);
                }
            }
            return ret;

        } else if (c instanceof OrCond and) {
            if (and.sons == null) {
                return 0f;
            }
            float ret = Float.MAX_VALUE;
            for (final Object son : and.sons) {
                final float estimate = estimateCost((Condition)son,previous);
                if (estimate != Float.MAX_VALUE) {
                    ret = (estimate < ret) ? estimate : ret;
                    if (ret == 0){
                        return 0f;
                    }
                }
            }
            return ret;
        } else if (c instanceof Terminal t) {
            return getConditionCost()[t.getId()];
        } else {
            return 0f;
        }
    }

    /**
     * Caching del contributo numerico
     */
    void setNumericContribution(int a, int b, float value) {
        if (hardcoreVersion) {
            numericContributionRaw[actionsArrayShifter.getTID(a)][termsArrayShifter.getTID(b)] = value;
        } else {
            numericContribution.put(Pair.of(actionsArrayShifter.getTID(a), termsArrayShifter.getTID(b)), value);
        }
    }

    public Float getNumericContribution(int a, int b) {
        if (hardcoreVersion) {
            return numericContributionRaw[actionsArrayShifter.getTID(a)][termsArrayShifter.getTID(b)];
        }
        return numericContribution.getOrDefault(Pair.of(actionsArrayShifter.getTID(a), termsArrayShifter.getTID(b)), Float.MAX_VALUE);
    }

    /**
     * Calcola il "contributo" di un'azione su una Comparison (positivo se la avvicina
     * alla soddisfazione, negativo se la allontana, UNKNOWNEFFECT se non determinabile).
     * Gli effetti costanti "increase/decrease" vengono accumulati; effetti con dipendenza
     * dallo stato o "assign" non numerico vengono marcati come UNKNOWNEFFECT.
     */
    protected float numericContribution(int t, Comparison comp) {

        if (cp.numericEffectFunction()[t] == null || cp.numericEffectFunction()[t].isEmpty()) {
            return 0f;
        }

        Float positiveness = getNumericContribution(t, comp.getId());
        if (positiveness == Float.MAX_VALUE) {
            positiveness = 0f;
            if (cp.numericEffectFunction()[t].isEmpty()) {
                setNumericContribution(t, comp.getId(), 0f);
                return positiveness;
            }
            if (comp.getLeft() instanceof ExtendedNormExpression extendedNormExpression) {
                final ExtendedNormExpression left = extendedNormExpression;
                for (final ExtendedAddendum ad : left.summations) {
                    // Termine binario: se tocca fluent coinvolti negli effetti => sconosciuto
                    if (ad.bin != null) {
                        for (final NumEffect ne : cp.numericEffectFunction()[t]) {
                            NumFluent fluentAffected = ne.getFluentAffected();
                            if (ad.bin.getInvolvedNumericFluents().contains(fluentAffected)) {
                                setNumericContribution(t, comp.getId(), UNKNOWNEFFECT);
                                return UNKNOWNEFFECT;
                            }
                        }
                    }
                    // Termine lineare su un fluente: accumula costante se effetto è costante
                    if (ad.f != null) {
                        for (final NumEffect ne : cp.numericEffectFunction()[t]) {

                            if (!ne.getFluentAffected().equals(ad.f)) {
                                continue;
                            }

                            if (ne.getInvolvedNumericFluents().isEmpty()) {
                                final ExtendedNormExpression rhs = (ExtendedNormExpression) ne.getRight();
                                if (!rhs.linear || !rhs.isNumber() || ne.getOperator().equals("assign")) {
                                    setNumericContribution(t, comp.getId(), UNKNOWNEFFECT);
                                    return UNKNOWNEFFECT;
                                }
                                if (ne.getOperator().equals("increase")) {
                                    positiveness += rhs.getNumber().floatValue() * ad.n.floatValue();
                                } else if (ne.getOperator().equals("decrease")) {
                                    positiveness += (-1) * rhs.getNumber().floatValue() * ad.n.floatValue();
                                }
                            } else {// Effetto dipendente dallo stato: marca come sconosciuto
                                setNumericContribution(t, comp.getId(), UNKNOWNEFFECT);
                                return UNKNOWNEFFECT;
                            }
                        }
                    }
                }
                setNumericContribution(t, comp.getId(), positiveness);
                return positiveness;
            } else {
                throw new RuntimeException("At the moment only normalized expressions are considered " + comp);
            }
        }
        return positiveness;
    }

    /**
     * Restituisce l'insieme di azioni potenziali, opzionalmente arricchito con helpful transitions.
     */
    @Override
    public Object[] getTransitions(final boolean helpful) {
        Collection res;
        if (helpfulActions == null || !helpful) {
            if (reachableTransitionsInstances == null) {
                if (reachableTransitions == null) {
                    res = getProblem().actions;
                } else {
                    reachableTransitionsInstances = new LinkedHashSet<TransitionGround>();
                    for (final int i : reachableTransitions) {
                        Transition transition = getTransition(cp.cpTr2TrMap()[i]);
                        if (transition.getSemantics().equals(Transition.Semantics.ACTION))
                            reachableTransitionsInstances.add((TransitionGround)transition);
                    }
                    reachableTransitionsInstances = new ArrayList<>(reachableTransitionsInstances);
                    res = reachableTransitionsInstances;
                }
            } else {
                res = reachableTransitionsInstances;
            }
        } else {
            res = helpfulActions;
        }
        if (helpfulTransitions) {
            res.addAll(getHelpfulTransitions());
        }
        return res.toArray();
    }

    /**
     * Azioni applicabili potenzialmente (o quelle iniziali se è attiva la memorizzazione).
     */
    public Collection<TransitionGround> getPotentialApplicableActions(){
        if (storeInitActions){
            return this.initActions;
        }else{
            return this.getAllTransitions();
        }
    }

    @Override
    public Collection<TransitionGround> getAllTransitions() {
        if (reachableTransitionsInstances == null) {
            if (reachableTransitions == null) {
                throw new RuntimeException("The heuristics should be called at least once to be used to get the reached actions");
            }
            reachableTransitionsInstances = new LinkedHashSet<TransitionGround>();
            for (final int i : reachableTransitions) {
                TransitionGround transition = (TransitionGround) getTransition(cp.cpTr2TrMap()[i]);
                if (transition.getSemantics().equals((Transition.Semantics.ACTION)))
                    reachableTransitionsInstances.add((TransitionGround) getTransition(cp.cpTr2TrMap()[i]));
            }
            reachableTransitionsInstances = new ArrayList<>(reachableTransitionsInstances);
            return reachableTransitionsInstances;
        } else {
            return reachableTransitionsInstances;
        }
    }

    /**
     * Estrae le helpful transitions  dal piano rilassato.
     */
    public Collection<Pair<TransitionGround, Integer>> getHelpfulTransitions() {
        if (!extractRelaxedPlan && !isHelpfulActionsComputation()) {
            throw new RuntimeException("Helpful Transitions can only be activatated in combination with the relaxed plan extraction");
        }
        Collection<Pair<TransitionGround, Integer>> res = new ArrayList<>();

        for (final int actionTransitionId : plan) {
            int actionId = cp.tr2CpTrMap()[actionTransitionId].iterator().next();
            if (getActionInit()[actionId]) {
                final IntArraySet right = repetitionsInThePlan[actionTransitionId];
                if (!right.isEmpty()) {
                    if (maxMRP) {
                        int max = 0;
                        for (int i : right) {
                            if (i > max) {
                                max = i;
                            }
                        }
                        if (max > 1) {
                            res.add(Pair.of((TransitionGround) getTransition(actionTransitionId), max));
                        }
                    } else {
                        int min = Integer.MAX_VALUE;
                        for (int i : right) {
                            if (i < min) {
                                min = i;
                            }
                        }
                        if (min > 1) {
                            res.add(Pair.of((TransitionGround) getTransition(actionTransitionId), min));
                        }
                    }
                }
            }
        }
        return res;
    }

    /**
     * Registra che un'azione può "cancellare/peggiorare" un Comparison
     */
    public void addDeleter(int i, int actId) {
        if (deleters[i] == null) {
            deleters[i] = new IntArraySet();
        }
        deleters[i].add(actId);
    }

    void updateDeleters(int t, int actionId) {
        addDeleter(t, actionId);
    }

    /**
     * Restituisce la formulazione del goal come condition
     */
    public Condition getGoalFormulation() {
        return cp.preconditionFunction()[cp.goal()];
    }

    /**
     * Restituisce l'insieme di condizioni terminali che possono essere
     * rese vere/peggiorate dall'azione data (proposizionali e numeriche).
     */
    protected IntSet getConditionsAchievableById(int actionId) {
        if (getConditionsAchievableBy()[actionId] == null) {
            final IntArraySet achievableTerms = new IntArraySet();
            final IntArraySet deletableTerms = new IntArraySet();
            // Condizioni numeriche che l'azione può migliorare
            for (final int t : getAllComparisons()) {
                final float v = this.numericContribution(actionId, (Comparison) Terminal.getTerminal(t));
                if (v > 0 || v == UNKNOWNEFFECT) {
                    achievableTerms.add(t);
                    updateAchievers(t, actionId);
                    if (DEBUG) {
                        System.out.println("Transition: " + getTransition(actionId));
                        System.out.println("Comparison Achievable: " + Terminal.getTerminal(t));
                        System.out.println("Numeric Contribution: " + v);
                    }
                } else {
                    // Se peggiora e sono attivi smart constraints, registra tra i deleters
                    if (v < 0 && useSmartConstraints) {
                        if (DEBUG) {
                            System.out.print(Transition.getTransition(actionId) + " worsens");
                            System.out.println((Comparison) Terminal.getTerminal(t));
                        }
                        updateDeleters(t, actionId);
                        deletableTerms.add(t);
                    }
                }
            }
            // Parte proposizionale: intersezione tra allConditions e effetti proposizionali dell'azione
            final Collection<Integer> propEff = cp.propEffectFunction()[actionId];
            if (propEff != null && !propEff.isEmpty()) {
                Sets.SetView<Integer> intersection = Sets.intersection(getAllConditions(), (Set<Integer>) propEff);
                achievableTerms.addAll(intersection);
                for (final int o : intersection) {
                    updateAchievers(o, actionId);
                }
            }
            conditionsAchievableBy[actionId] = achievableTerms;
            if (useSmartConstraints)
                conditionsDeletableBy[actionId] = deletableTerms;

        }
        return getConditionsAchievableBy()[actionId];
    }

    /**
     * Numero di ripetizioni di un'azione con contributo v necessario
     * per portare a vero un confronto numerico Terminal t nello stato s.
     */
    private float computeRepetition(Terminal t, double v, State s) {
        final double eval = ((Comparison) t).getLeft().eval(s);
        if (Double.isNaN(eval)){
            return 1.0f;
        }
        // Se confronto è stretto e sommiamo (h_add), proteggiamo da arrotondamenti
        if (((Comparison) t).isStrict && this.isAdditive()){
            return (float) (-1f * eval / v)+Float.MIN_VALUE;
        }
        return (float) (-1f * eval / v);
    }

    protected void cacheValue(float rep, int actionId, Terminal t) {

    }

    protected boolean update(Terminal t, boolean update, int actionId) {
        return update;
    }

    // UTILITY

    /**
     * @return allAchievers (allocato on-demand se nullo)
     */
    public IntArraySet[] getAllAchievers() {
        if (allAchievers == null){
            allAchievers = new IntArraySet[getTotNumberOfTerms()];
        }
        return allAchievers;
    }

    public int getTotNumberOfTerms() {
        return totNumberOfTerms;
    }

    public int getTotNumberOfTermsRefactored() {
        return totNumberOfTermsRefactored;
    }

    public PDDLProblem getProblem() {
        return problem;
    }

    public boolean isHelpfulActionsComputation() {
        return helpfulActionsComputation;
    }

    public IntArraySet[] getConditionsAchievableBy() {
        return conditionsAchievableBy;
    }

    public IntArraySet[] getConditionsDeletableBy() {
        return conditionsDeletableBy;
    }

    public IntArraySet[] getConditionToAction() {
        return conditionToAction;
    }

    public IntArraySet getAllConditions() {
        return allConditions;
    }

    public IntArraySet[] getReachableAchievers() {
        return allAchievers;
    }

    public IntArraySet getAllComparisons() {
        return allComparisons;
    }

    public FibonacciHeapNode[] getNodeOf() {
        return nodeOf;
    }

    public boolean isReachability() {
        return reachability;
    }

    public boolean isConjunctionsMax() {
        return conjunctionsMax;
    }

    public float[] getActionCost() {
        return cp.actionCost();
    }

    public float[] getActionHCost() {
        return actionHCost;
    }

    public float[] getConditionCost() {
        return conditionCost;
    }

    public boolean[] getClosed() {
        return closed;
    }

    public boolean isAdditive() {
        return additive;
    }

    public boolean[] getConditionInit() {
        return conditionInit;
    }

    public boolean[] getActionInit() {
        return actionInit;
    }

    public void setComputeHelpfulActionsMap(){
        isHelpfulMap = true;
    }

    // METODI PER INTERFERENCE FREE
    public boolean computeInterferenceFree() {
        // Assicura che gli achievers diretti siano calcolati per tutte le azioni,
        // altrimenti le strutture usate dal controllo IF restano vuote.
        ensureAchieversComputed();
        this.indirectAchievers = calculateIndirectAchievers();
        this.isDomainInterferenceFree = isProblemInterferenceFree();
        return isDomainInterferenceFree;
    }

    /**
     * Calcola gli Indirect Achievers (IAch) per tutte le condizioni numeriche (Comparison).
     */
    private IntArraySet[] calculateIndirectAchievers() {
        // 1. inizializzazione con gli Achievers diretti (Ach)
        final IntArraySet[] directAchievers = getAllAchievers();
        final IntArraySet[] indirectAchievers = new IntArraySet[totNumberOfTerms];

        final IntArraySet numericConds = getAllComparisons();
        for (int termId : numericConds) {
            IntArraySet direct = directAchievers[termId];
            indirectAchievers[termId] = (direct == null) ? new IntArraySet() : new IntArraySet(direct);
        }

        boolean changes = true;
        // propagazione all'indietro, continua finché in un'iterazione vengono aggiunte nuove azioni
        while (changes) {
            changes = false;

            for (int termId : numericConds) {
                final IntSet currentIAch = indirectAchievers[termId];
                if (currentIAch == null || currentIAch.isEmpty()) continue;

                // set temporaneo per tracciare le nuove aggiunte
                final IntArraySet newAdditions = new IntArraySet();

                // itera sull'IAch corrente (a')
                for (int aPrimeId : currentIAch) {
                    final IntSet precondTerminals = actionPreconditionTerminals[aPrimeId];
                    if (precondTerminals == null || precondTerminals.isEmpty()) continue;

                    for (int preId : precondTerminals) {
                        if (!numericConds.contains(preId)) continue; // considera solo precondizioni numeriche

                        final IntArraySet achieversOfTerminal = directAchievers[preId];
                        if (achieversOfTerminal == null) continue;

                        // per ogni azione a in Ach(t)
                        for (int aId : achieversOfTerminal) {
                            // aggiungi se non è già presente in IAch(termId)
                            if (!currentIAch.contains(aId)) {
                                newAdditions.add(aId);
                            }
                        }
                    }
                }

                // Aggiorna l'insieme originale solo con le nuove aggiunte
                if (!newAdditions.isEmpty()) {
                    currentIAch.addAll(newAdditions);
                    changes = true;
                }
            }
        }

        return indirectAchievers;
    }


    /**
     * Pre-calcola tutte le coppie di azioni (a_i, a_j) che sono Achievers diretti (Ach)
     * per la stessa condizione numerica psi
     * Restituisco: Mappa da coppia di Azioni all'ID della psi che fa co-achieve
     */
    private Map<Entry<Integer, Integer>, Integer> precomputeCoAchievers(IntArraySet[] directAchievers) {
        Map<Entry<Integer, Integer>, Integer> coAchievers = new HashMap<>();

        for (int psiId : getAllComparisons()) {
            final IntArraySet achievers = directAchievers[psiId];
            if (achievers == null || achievers.size() < 2) continue;

            // converte in array per iterare in modo efficiente
            int[] achArray = achievers.toIntArray();

            // itera su tutte le coppie di achievers (a_i, a_j) per questa psi
            for (int i = 0; i < achArray.length; i++) {
                for (int j = i + 1; j < achArray.length; j++) {
                    int aiId = achArray[i];
                    int ajId = achArray[j];

                    // usa il metodo helper per creare la chiave simmetrica
                    Entry<Integer, Integer> pair = getSymmetricKey(aiId, ajId);

                    // mappa la coppia al primo psi trovato
                    if (!coAchievers.containsKey(pair)) {
                        coAchievers.put(pair, psiId);
                    }
                }
            }
        }
        return coAchievers;
    }


    /**
     * Verifica se l'azione a_i interferisce con a_j
     */
    private int findInterferingNumericCondition(int aiId, int ajId,
                                                IntArraySet[] indirectAchievers,
                                                Map<Entry<Integer, Integer>, Integer> coAchieversMap) {
        if (aiId == ajId) return -1;

        // 1. a_i in IAch(psi') per qualche psi' in pre(a_j)
        boolean aiIsIndirectAchiever = false;
        final IntSet ajPreconditions = actionPreconditionTerminals[ajId];

        if (ajPreconditions != null) {
            for (int precondId : ajPreconditions) {
                if (!getAllComparisons().contains(precondId)) continue;
                IntSet iAchSet = indirectAchievers[precondId];
                if (iAchSet != null && iAchSet.contains(aiId)) {
                    aiIsIndirectAchiever = true;
                    break;
                }
            }
        }

        if (!aiIsIndirectAchiever) {
            return -1; // fallisce la Condizione 1
        }

        // 2. esiste psi tale che a_i in Ach(psi) E a_j in Ach(psi)

        // crea la chiave simmetrica
        Entry<Integer, Integer> pair = getSymmetricKey(aiId, ajId);

        // cerca nella mappa pre-calcolata
        Integer psiId = coAchieversMap.get(pair);

        return (psiId != null) ? psiId : -1;
    }

    /**
     * Valuta se il problema è interference-free
     */
    private boolean isProblemInterferenceFree() {
        ensureAchieversComputed();
        // calcola le dipendenze una sola volta
        final IntArraySet[] directAchievers = getAllAchievers();
        final IntArraySet[] indirectAchievers = calculateIndirectAchievers();

        // pre-calcolo usando Map.Entry
        final Map<Entry<Integer, Integer>, Integer> coAchieversMap = precomputeCoAchievers(directAchievers);

        // itera su tutte le coppie di azioni distinte (a_i, a_j)
        for (int aiId : allActions) {
            // Salta se a_i è l'azione Goal fittizia
            if (isGoalAction(aiId)) {
                continue;
            }

            for (int ajId : allActions) {
                // Salta se a_j è l'azione Goal fittizia
                if (isGoalAction(ajId)) {
                    continue;
                }

                if (aiId == ajId) {
                    continue;
                }

                // controlla se a_i interferisce con a_j
                int interferingPsiId = findInterferingNumericCondition(aiId, ajId, indirectAchievers, coAchieversMap);

                // se a_i interferisce con a_j
                if (interferingPsiId != -1) {

                    // verifico la condizione di Interference-Free: pre(a_i) implica pre(a_j)
                    if (!checkPreconditionImplication(aiId, ajId)) {
                        System.out.println("VIOLAZIONE IF RILEVATA (implicazione non verificata):");
                        System.out.println("  Coppia azioni: (" + formatAction(aiId) + ", " + formatAction(ajId) + ") [" + aiId + ", " + ajId + "]");
                        System.out.println("  Psi co-achieved: " + formatTerminal(interferingPsiId) + " [id=" + interferingPsiId + "]");
                        String preWithIAch = findOneIndirectPrecondition(aiId, ajId);
                        if (preWithIAch != null) {
                            System.out.println("  Ai è in IAch di una precondizione di aj: " + preWithIAch);
                        }

                        // dettaglio precondizioni
                        System.out.println("  pre(ai): " + formatPreconditions(aiId));
                        System.out.println("  pre(aj): " + formatPreconditions(ajId));
                        return false;
                    }
                }

                // 2. se a_i influenza direttamente una precondizione
                //    di a_j, allora occorre comunque che
                //    pre(a_i) => pre(a_j)
                if (affectsAnyPrecondition(aiId, ajId)) {
                    if (!checkPreconditionImplication(aiId, ajId)) {
                        System.out.println("VIOLAZIONE IF RILEVATA (ai peggiora una precond di aj e non la implica):");
                        System.out.println("  Coppia azioni: (" + formatAction(aiId) + ", " + formatAction(ajId) + ") [" + aiId + ", " + ajId + "]");
                        System.out.println("  Precondizioni numeriche di aj peggiorate da ai:");
                        System.out.println("  pre(ai): " + formatPreconditions(aiId));
                        System.out.println("  pre(aj): " + formatPreconditions(ajId));
                        return false;
                    }
                }

                // 3. se ai e aj co-achievano una psi e almeno uno dei due richiede psi come precondizione,
                //    l'implicazione fra precondizioni deve essere vera
                Entry<Integer, Integer> pair = getSymmetricKey(aiId, ajId);
                Integer psiId = coAchieversMap.get(pair);
                if (psiId != null) {
                    final IntSet pre_ai = actionPreconditionTerminals[aiId];
                    final IntSet pre_aj = actionPreconditionTerminals[ajId];
                    boolean psiRequired = (pre_ai != null && pre_ai.contains(psiId)) || (pre_aj != null && pre_aj.contains(psiId));
                    if (psiRequired) {
                        if (!checkPreconditionImplication(aiId, ajId)) {
                            System.out.println("VIOLAZIONE IF RILEVATA (Co-achievers su psi che è anche precondizione):");
                            System.out.println("  Coppia azioni: (" + formatAction(aiId) + ", " + formatAction(ajId) + ") [" + aiId + ", " + ajId + "]");
                            System.out.println("  psi: " + formatTerminal(psiId) + " [id=" + psiId + "] è richiesta come precondizione da almeno una delle due azioni");
                            System.out.println("  pre(ai): " + formatPreconditions(aiId));
                            System.out.println("  pre(aj): " + formatPreconditions(ajId));
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Verifica se la precondizione di a_i implica logicamente la precondizione di a_j.
     */
    private boolean checkPreconditionImplication(int aiId, int ajId) {
        // pre(a_i)
        final IntSet pre_ai_terminals = actionPreconditionTerminals[aiId];
        // pre(a_j)
        final IntSet pre_aj_terminals = actionPreconditionTerminals[ajId];

        // se pre(a_j) è vuoto, è sempre implicato.
        if (pre_aj_terminals == null || pre_aj_terminals.isEmpty()) {
            return true;
        }

        // se pre(a_j) non è vuoto e pre(a_i) è vuoto, non c'è implicazione
        if (pre_ai_terminals == null || pre_ai_terminals.isEmpty()) {
            return false;
        }

        // verifica se pre(a_j) è un sottoinsieme di pre(a_i) (uso di containsAll)
        return pre_ai_terminals.containsAll(pre_aj_terminals);
    }

    // METODI HELPER
    /**
     * Popola gli achievers per tutte le azioni visitando gli effetti delle azioni.
     */
    private void ensureAchieversComputed() {
        if (allActions == null || allActions.isEmpty()) return;
        for (int aId : allActions) {
            getConditionsAchievableById(aId);
        }
    }

    /**
     * Verifica se l'azione a_i ha un effetto potenzialmente interferente su almeno una
     * precondizione di a_j
     */
    private boolean affectsAnyPrecondition(int aiId, int ajId) {
        final IntSet pre_aj_terminals = actionPreconditionTerminals[ajId];
        if (pre_aj_terminals == null || pre_aj_terminals.isEmpty()) return false;

        // Solo parte numerica: se una precondizione numerica è peggiorata da a_i
        // Se viene trovata anche una sola precondizione numerica di aj che viene peggiorata da un effetto diretto di ai,
        // il metodo restituisce true
        for (int termId : pre_aj_terminals) {
            if (allComparisons.contains(termId)) {
                final Terminal t = Terminal.getTerminal(termId);
                if (t instanceof Comparison cmp) {
                    final float v = this.numericContribution(aiId, cmp);
                    // Considera interferenza solo se peggiora la condizione richiesta
                    if (!Float.isNaN(v) && v < 0f) return true;
                }
            }
        }

        return false;
    }

    private boolean isGoalAction(int actionId) {
        final int lastActionId = allActions.size() - 1;

        return actionId == lastActionId;
    }

    /**
     * Mi assicuro che (aiId, ajId) e (ajId, aiId) generino la stessa chiave
     */
    private Entry<Integer, Integer> getSymmetricKey(int a, int b) {
        int key1 = Math.min(a, b);
        int key2 = Math.max(a, b);
        // SimpleImmutableEntry garantisce un hash code e un equals corretti per la mappa
        return new AbstractMap.SimpleImmutableEntry<>(key1, key2);
    }

    // METODI PER LOG
    private String formatAction(int aId) {
        try {
            if (aId == cp.goal()) return "GOAL";
            return String.valueOf(Transition.getTransition(cp.cpTr2TrMap()[aId]));
        } catch (Throwable t) {
            return "Action#" + aId;
        }
    }

    private String formatTerminal(int termId) {
        try {
            return String.valueOf(Terminal.getTerminal(termId));
        } catch (Throwable t) {
            return "Terminal#" + termId;
        }
    }

    private String formatPreconditions(int aId) {
        final IntSet pre = actionPreconditionTerminals[aId];
        if (pre == null || pre.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int t : pre) {
            if (!first) sb.append(", ");
            sb.append(formatTerminal(t)).append("[id=").append(t).append("]");
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }

    private String findOneIndirectPrecondition(int aiId, int ajId) {
        if (indirectAchievers == null) return null;
        final IntSet pre_aj = actionPreconditionTerminals[ajId];
        if (pre_aj == null || pre_aj.isEmpty()) return null;
        for (int preId : pre_aj) {
            IntSet iAch = indirectAchievers[preId];
            if (iAch != null && iAch.contains(aiId)) {
                return formatTerminal(preId) + "[id=" + preId + "]";
            }
        }
        return null;
    }

}