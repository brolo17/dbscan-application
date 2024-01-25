% Projet Version Logique (Prolog) CSI2510
% Winter 2022
%
% Student Name: Francesco Monti
% Student Number: 300177975



% Prédicat qui va créer la base de faits des partitions
import:-
    csv_read_file('partition65.csv', Data65, [functor(partition)]),maplist(assert, Data65),
    csv_read_file('partition74.csv', Data74, [functor(partition)]),maplist(assert, Data74),
    csv_read_file('partition75.csv', Data75, [functor(partition)]),maplist(assert, Data75),
    csv_read_file('partition76.csv', Data76, [functor(partition)]),maplist(assert, Data76),
    csv_read_file('partition84.csv', Data84, [functor(partition)]),maplist(assert, Data84),
    csv_read_file('partition85.csv', Data85, [functor(partition)]),maplist(assert, Data85),
    csv_read_file('partition86.csv', Data86, [functor(partition)]),maplist(assert, Data86),listing(partition).




%% Prédicats Pour Extraire Un Groupe De Points De La Liste Originale %%
%% Retire Le Premier Element De La Liste et Tous Les Autres Points Appartenant Au Même Groupe %%

% removeAll/4
% Remove From A Given List All Elements With Given ClusterID
% Returns List Of Removed Elements And Of Remaining Elements
% removeAll(clusterID,listIn,listRemovedOut,listRemainingOut)
removeAll(_,[],[],[]).
removeAll(C,[[D,X,Y,C]|Z],[[D,X,Y,C]|Z2],Z3) :- removeAll(C,Z,Z2,Z3), !.
removeAll(C,[[D,X,Y,C2]|Z],Z2,[[D,X,Y,C2]|Z3]) :- removeAll(C,Z,Z2,Z3).

% extractCluster/3
% Extract Cluster Using The ClsterID Of The First Element In The Given List
% Returns The Extracted Cluster And The Remaining List
% extractCluster(clusterListIn,clusterOut,listRemainingOut)
extractCluster([[D,X,Y,C]|Z],Z2,Z3) :- removeAll(C,[[D,X,Y,C]|Z],Z2,Z3).




%% Prédicats Pour Calculer l’Intersection Entre Un Groupe Et Une Liste De Groupes %%
%% Retournent La Liste Des 'clusterIDs' De Groupes Ayant Un Point En Commun Avec Le Groupe Donné %%

% containsPoint/3
% Returns a List of 'clusterIDs' that Contains the Given Point
% Checks the Clusters inside a given List
% containsPoint(pointIn,clusterListIn,clusterIDsListOut)
containsPoint(_,[],[]).
containsPoint([D,X,Y,C],[[D2,_,_,_]|Z],Z2) :- dif(D,D2),containsPoint([D,X,Y,C],Z,Z2).
containsPoint([D,X,Y,C],[[D,_,_,C2]|Z],[C2|Z2]) :- containsPoint([D,X,Y,C],Z,Z2).

% clusterIntersection/3
% Returns the List of 'clusterIDs' that Intersect with the Given Cluster
% Makes a call to help predicate clusterIntersection/4
% Does not remove duplicates
% clusterIntersection(clusterIn,clusterListIn,clusterIDsListOut)
clusterIntersection([[D,X,Y,C]|Z],Z2,Res) :- clusterIntersection([[D,X,Y,C]|Z],Z2,[],Res).

% clusterIntersection/4
% Help Predicate For clusterIntersection/3
% clusterIntersection(clusterIn,clusterListIn,helperList,clusterIDsListOut)
clusterIntersection([],_,Res,Res).
clusterIntersection([[D,X,Y,C]|Z],Z2,Z3,Res) :- containsPoint([D,X,Y,C],Z2,O),
                                                append(Z3,O,Z4),
                                                clusterIntersection(Z,Z2,Z4,Res).




%% Prédicats Pour Changer Les 'clusterIDs' De Groupes Pour Un Autre %%
%% Retorune Une Nouvelle Liste De Listes Avec Les IDs Changés %%

% replaceElements/4
% Replaces the elements O with R in a given list
% replaceElements(O,R,listIn,listOut)
replaceElements(_,_,[],[]).
replaceElements(O,R,[O|L],[R|L2]) :- replaceElements(O,R,L,L2).
replaceElements(O,R,[H|L],[H|L2]) :- dif(H,O),replaceElements(O,R,L,L2).

% relabel/4
% Relabels the points of cluster O with label R
% relabel(O,R,clusterListIn,clusterListOut)
relabel(_,_,[],[]).
relabel(O,R,[[X|Y]|Z],[Res|Z2]) :- replaceElements(O,R,[X|Y],Res),relabel(O,R,Z,Z2).

% relabelAll/4
% Relabels the points with 'clusterID' inside a given list with the given ID
% relabelAll(clusterIDListIn,newClusterID,clusterListIn,clusterListOut)
relabelAll([],_,Res,Res2) :- copy_term(Res,Res2).
relabelAll([X|Y],C,Z,Res2) :- relabel(X,C,Z,Res),relabelAll(Y,C,Res,Res2).




%% Prédicats Principales %%
%% Prédicats Pour Retourner Une Liste Avec Des Groupes Fusionnés %%

% mergeClusters/2
% Predicate called by the user
% Uses mergeClusters/3 to Return the list of clusters after merging
% mergeClusters(clusterListIn,mergedClusterListOut)
mergeClusters(L,Result) :- copy_term(L,L1),copy_term(L,L2),mergeClusters(L1,L2,Result).

% mergeClusters/3
% Actually executes the merging algorithm
% RelabelAll working on L, extractCluster + clusterIntersection working on a clone of L
% It runs the 'clusterIntersection' on L2, a copy of the given list L
% L2 does not include the clusters already processed
% Running it on the original list L gives another result
% Both solutions are probably acceptable, but I left the intersection on L2 to match the solution given by the professor
% mergeClusters(clusterListIn,copyClustersListIn,mergedClusterListOut)
mergeClusters(Result,[],Result).
mergeClusters(L,L2,Result) :- extractCluster(L2,K,L3),
                       clusterIntersection(K,L2,J),
                       getC(K,C),relabelAll(J,C,L,L4),
                       mergeClusters(L4,L3,Result).

% getC/2
% Returns the 'clusterID' of a the first point of a given list of clusters
% getC(clusterListIn,firstPointClusterID)
getC([[D,X,Y,C]|Z],C).



%% Line To Execute Algorithm %%
%% Prints Result in a '.txt' File Called 'clusters.txt' %%
%% findall([D,X,Y,C],partition(_,D,X,Y,C),L),mergeClusters(L,Result),open('clusters.txt',write,F),write(F,Result),close(F). %%




%% TEST PREDICATES %%

% removeAll/4
test(removeAll) :- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),removeAll(76000001,L,Removed,Remaining)'),
                   nl,findall([D,X,Y,C],partition(_,D,X,Y,C),L),removeAll(76000001,L,Removed,Remaining),
                   write("Removed: "),write(Removed),nl,
                   write("Remaining: "),write(Remaining).

% extractCluster/3
test(extractCluster) :- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),extractCluster(L,Cluster,Remaining)'),
                        nl,findall([D,X,Y,C],partition(_,D,X,Y,C),L),extractCluster(L,Cluster,Remaining),
                        write("Cluster: "),write(Cluster),nl,
                        write("Remaining List: "),write(Remaining).

% containsPoint/3
test(containsPoint) :- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),containsPoint([155865, 40.759827, -73.93685, 86000001],L,Result)'),
                       nl,findall([D,X,Y,C],partition(_,D,X,Y,C),L),containsPoint([155865, 40.759827, -73.93685, 86000001],L,Result),
                       write(Result).

% clusterIntersection/3
test(clusterIntersection) :- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),extractCluster(L,K,L2),clusterIntersection(K,L2,Result)'),
                             nl,findall([D,X,Y,C],partition(_,D,X,Y,C),L),extractCluster(L,K,L2),clusterIntersection(K,L2,Result),
                             write(Result).

% clusterIntersection/4
test(clusterIntersection2) :- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),extractCluster(L,K,L2),clusterIntersection(K,L2,[],Result)'),
                              nl,findall([D,X,Y,C],partition(_,D,X,Y,C),L),extractCluster(L,K,L2),clusterIntersection(K,L2,[],Result),
                              write(Result).

% replaceElements/4
test(replaceElements) :- write('replaceElements(33,77,[1,2.2,3.1,33],Result)'),
                         nl,replaceElements(33,77,[1,2.2,3.1,33],Result),
                         write(Result).

% relabel/4
test(relabel) :- write('relabel(33,77,[[1,2.2,3.1,33],[2,2.1,3.1,22],[3,2.5,3.1,33],[4,2.1,4.1,33],[5,4.1,3.1,30]],Result)'),
                 nl,relabel(33,77,[[1,2.2,3.1,33],[2,2.1,3.1,22],[3,2.5,3.1,33],[4,2.1,4.1,33],[5,4.1,3.1,30]],Result),
                 write(Result).

% relabelAll/4
test(relabelAll) :- write('relabelAll([33,30],77,[[1,2.2,3.1,33],[2,2.1,3.1,22],[3,2.5,3.1,33],[4,2.1,4.1,33],[5,4.1,3.1,30]],Result)'),
                    nl,relabelAll([33,30],77,[[1,2.2,3.1,33],[2,2.1,3.1,22],[3,2.5,3.1,33],[4,2.1,4.1,33],[5,4.1,3.1,30]],Result),
                    write(Result).

% mergeClusters/2
test(mergeClusters) :- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),mergeClusters(L,Result)'),
                       nl,findall([D,X,Y,C],partition(_,D,X,Y,C),L),mergeClusters(L,Result),
                       write(Result).

% mergeClusters/3
test(mergeClusters2) :- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),copy_term(L,L2),mergeClusters(L,L2,Result)'),
                        nl,findall([D,X,Y,C],partition(_,D,X,Y,C),L),copy_term(L,L2),mergeClusters(L,L2,Result),
                        write(Result).

% getC/2
test(getC) :- write('findall([D,X,Y,C],partition(_,D,X,Y,C),L),getC(L,Result)'),
              nl,findall([D,X,Y,C],partition(_,D,X,Y,C),L),getC(L,Result),
              write(Result).
