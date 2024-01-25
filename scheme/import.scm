#lang scheme

; Projet Version Fonctionnelle (Scheme) CSI2510
; Winter 2022
;
; Student Name: Francesco Monti
; Student Number: 300177975



; Fonction Pour Ouvrir et Lire un Fichier Avec le Nom Donné
; 'filename', le nom du fichier à ouvrir
(define (readlist filename)
 (call-with-input-file filename
  (lambda (in)
    (read in))))



; Fonction Utilisée Pour Créer une Liste Contenant Tous les Points de Toutes les Partitions
; Les Partitions Actuellement Utilisées Sont : 64 - 74 - 75 - 76 - 84 - 85 - 86
;
; La Fonction Retourne une Liste Avec des Points du Type : (partitionID,pointID,X,Y,clusterID)
;
; Deux Types de (mergeClusters) Seront Implémentés, un Travaillant Avec ce Type de Points, et un Travaillant Avec la Structure Préférée: (pointID,X,Y,clusterID)
(define (import)
  (let ((p65 (readlist "partition65.scm"))
        (p74 (readlist "partition74.scm")) 
        (p75 (readlist "partition75.scm"))
        (p76 (readlist "partition76.scm"))
        (p84 (readlist "partition84.scm"))
        (p85 (readlist "partition85.scm"))
        (p86 (readlist "partition86.scm")))
    (append p65 p74 p75 p76 p84 p85 p86)))



; Fonction Qui Retourne le 'pointID' d'un Point de Type : (partitionID,pointID,X,Y,clusterID)
; 'point', le point pour lequel son 'pointID' sera retourné
(define (getD point)
  (car (cdr point)))



; Fonction Qui Retourne le 'clusterID' d'un Point de Type : (partitionID,pointID,X,Y,clusterID)
; 'point', le point pour lequel son 'clusterID' sera retourné
(define (getC point)
  (car (reverse point)))



; Fonction Qui Retourne une Liste Répresentante la Différence de Deux Listes Données 'list1' et 'list2'
;
; Pour Chaque Elément de 'list2', la Fonction Vérifie si 'list1' Contient une Occurrence de cet Elément
; Si c'est le Cas, l'Elément Sera Supprimé de la 'list1', Sinon Rien ne se Passera.
;
; La Fonction ne Supprime qu'une Seule Occurrence de Chaque Elément Présent Dans 'list2'
; Par Exemple : (subtractLists '(1 1 2) '(1 3)) Retournera (1 2) et Non (2)
;
; 'list1', la liste à partir de laquelle les éléments seront soustraits
; 'list2', la liste contenant les éléments à soustraire
(define (subtractLists list1 list2)
  (if (null? list2)
      list1 (subtractLists (remove (car list2) list1) (cdr list2))))



; Fonction Qui Extrait un Groupe de Points du Type (partitionID,pointID,X,Y,clusterID) de la Liste Originale
; La Fonction Extrait le Cluster du Premier Point de la Liste Données
;
; 'initialList', la liste à partir de laquelle extraire le cluster 
(define (extractCluster initialList)

    ;; Secondary Function that extracts the cluster with the given 'clusterID'
    ;; Takes as parameters:
    ;; 'C', the 'clusterID' of the cluster to be extracted
    ;; 'initialList', the original list from where the cluster will be extracted
    ;; 'cluster', a list containing the points of the extracted cluster - most probably an empty list at the beginning 
    (define (getCluster C initialList cluster) 
      (cond
        ((null? initialList) '())
        ((= C (getC (car initialList))) (cons (car initialList) (getCluster C (cdr initialList) cluster)))
        (else (getCluster C (cdr initialList) cluster))))

  ;;; Call the secondary function with the clusterID of the first point of the list
  (let ((C (getC (car initialList)))) (getCluster C initialList '())))



; Fonction Qui Calcule l’Intersection Entre un Groupe et une Liste de Groupes
; Elle Retourne la Liste des clusterIDs de Groupes Ayant un Point en Commun Avec ce Groupe
;
; 'cluster', le groupe pour lequel trouver l'intersection
; 'listOfClusters', la liste de groupes utilisée pour trouver l'intersection
;
; La Fonction ne Supprime pas les Copies
(define (clusterIntersection cluster listOfClusters)

    ;; Secondary Function that returns the clusterIDs of clusters of a given list that contain a given point
    ;; Takes as parameters:
    ;; 'point', the point for which the list of clusterIDs that contain it will be returned
    ;; 'listOfClusters', the list of clusters that will be checked if they contain the given point or not
    (define (containsPoint point listOfClusters)
      (cond
        ((null? listOfClusters) '())
        ((= (getD point) (getD (car listOfClusters))) (cons (getC (car listOfClusters)) (containsPoint point (cdr listOfClusters))))
        (else (containsPoint point (cdr listOfClusters)))))

  ;;; Builds the list of 'clusterIDs' representing the intersection by making a recursive call to 'clusterIntersection' and a call to the secondary function
  (cond
    ((null? cluster) '())
    (else (append (containsPoint (car cluster) listOfClusters) (clusterIntersection (cdr cluster) listOfClusters)))))



; Fonction qui Change les IDs de Groupes Pour un Autre
;
; 'IDs', liste contenant les 'clusterIDs' qui doivent etre changés
; 'C', le nouveau 'clusterID'
; 'listOfClusters', la liste dans laquelle les 'clusterIDs' vont etre changés
(define (relabelAll IDs C listOfClusters)

    ;; Secondary Function that sets the 'clusterID' of the points inside the given list with the given 'clusterID' to the given new 'clusterID'
    ;; Takes as parameters:
    ;; 'ID', the old 'clusterID'
    ;; 'C', the new 'clusterID'
    ;; 'listOfClusters', the list of points where the 'clusterID' will be changed
    (define (relabel ID C listOfClusters)
      (cond
        ((null? listOfClusters) '())
        ((= ID (getC (car listOfClusters))) (cons (reverse (append C (cdr (reverse (car listOfClusters))))) (relabel ID C (cdr listOfClusters))))
        (else (cons (car listOfClusters) (relabel ID C (cdr listOfClusters))))))

  ;;; Changes the 'clusterIDs' inside the given list by making a recursive call to 'relabelAll' and a call to the secondary function
  (when (not (null? IDs))
    (let ((res (relabel (car IDs) C listOfClusters)))
      (cond
        ((null? (cdr IDs)) res)
        (else (relabelAll (cdr IDs) C res))))))



; Fonction qui Retourne la Liste des Groupes Fusionnés Avec des Points du Type: (pointID,X,Y,clusterID)
; 'list', la liste de groupes à être fusionnés
(define (mergeClusters list)

    ;; First secondary funtion that actually executes the merge alghorithm
    ;; It runs the 'clusterIntersection' on 'list2', which does not include the clusters already processed
    ;; Running it on the original list gives another result
    ;; Both solutions are probably acceptable - I left the intersection on 'list2' to match the solution given by the professor
    ;;
    ;; Takes as parameters:
    ;; 'list', the original list where the clusters will be merged
    ;; 'list2', initially a copy of the original list - every time the function runs, the points of the extracted cluster will be removed from 'list2'
    (define (mergeClusters-v2 list list2)
      (let ((res (relabelAll (clusterIntersection (extractCluster list2) list2) (cons (getC (car (extractCluster list2))) '()) list)) (newList (subtractLists list2 (extractCluster list2))))
        (cond
          ((null? newList) res)
          (else (mergeClusters-v2 res newList)))))

    ;; Second secondary function that cleans the output by removing the 'partitionID' from every point of the given list
    (define (cleanOutput out)
      (cond
        ((null? out) '())
        (else (cons (cdr (car out)) (cleanOutput (cdr out))))))

  
  ;;; Builds the list after merging by making a call to 'mergeClusters-v2' with the given list twice as the two parameters
  ;;; Also make a call to cleanOutput in order to remove the 'partitionID' from the points and return a list of points of the type:
  ;;; (pointID,X,Y,clusterID)
  (cleanOutput (mergeClusters-v2 list list)))



; Fonction Pour Sauvegarder une Liste Dans un Fichier
; 'filename', le nom du fichier qui va etre créé
; 'L', la liste à sauvegarder dans le fichier
(define (saveList filename L)
 (call-with-output-file filename
  (lambda (out)
    (write L out))))



; Same function of 'mergeClusters', but that does not clean the output and return a list of poitns of the type: (partitionID,pointID,X,Y,clusterID)
; Still runs the cluster intersection on 'list2'
(define (mergeClusters-partitionID list)  
    (define (mergeClusters-v2 list list2)
      (let ((res (relabelAll (clusterIntersection (extractCluster list2) list2) (cons (getC (car (extractCluster list2))) '()) list)) (newList (subtractLists list2 (extractCluster list2))))
        (cond
          ((null? newList) res)
          (else (mergeClusters-v2 res newList)))))
  (mergeClusters-v2 list list))