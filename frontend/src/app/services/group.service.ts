import { Injectable } from "@angular/core";
import { map, Observable, of, tap } from "rxjs";
import { AuthService } from "./auth.service";
import { HttpClient } from "@angular/common/http";
import { Globals } from "../global/globals";
import {Group, GroupDetail, GroupWithPermission, Permission} from "../dtos/group";
import {User} from "../dtos/user";
import {Component as Comp} from "../dtos/component";

@Injectable({
  providedIn: "root",
})
export class GroupService {
  private groupBaseUri: string = this.globals.backendUri + "/group";
  private groupMemberUri: string = this.groupBaseUri + "/member";
  private groupBoardUri: string = this.groupBaseUri + "/board";

  //true = write, false = read, null = owner

  constructor(
    private httpClient: HttpClient,
    private globals: Globals,
    private authService: AuthService
  ) {}

  // returns group with groupId, groupName, owner (displayname und username)
  createGroup(): Observable<Group> {
    return this.httpClient.post<Group>(this.groupBaseUri, null)
  }

  // returns updatedName in group
  updateGroupName(group: Group): Observable<Group> {
    return this.httpClient.put<Group>(this.groupBaseUri + "/name", group)
  }

  setGroupOwner(groupId: number, username: string): Observable<Group> {
    return this.httpClient.put<Group>(this.groupBaseUri + "/owner", {id: groupId, username})
  }

  //
  addGroupMember(groupId: number, username: string) {
    return this.httpClient.post(this.groupMemberUri, {id: groupId, username})
  }

  //
  deleteGroupMember(groupId: number, username: string) {
    return this.httpClient.delete(this.groupMemberUri, {
      body: {id: groupId, username}
    })
  }

  //
  addGroupToBoard(groupWithPermission:GroupWithPermission) {
    return this.httpClient.post(this.groupBoardUri, groupWithPermission)
  }

  //
  removeGroupFromBoard(groupId: number, boardId: number) {
    return this.httpClient.delete(this.groupBoardUri, {
      body: {groupId, boardId}
    })
  }

  updateBoardPermission(groupWithPermission: GroupWithPermission) {
    return this.httpClient.put<GroupWithPermission>(this.groupBoardUri, groupWithPermission)
  }

  // returns (alle Users(username & displayName),groupId, groupOwner, groupName)
  getGroup(groupId: number): Observable<GroupDetail> {
    return this.httpClient.get<GroupDetail>(this.groupBaseUri + "/" + groupId)
  }

  // returns (alle Gruppen (mit groupId, groupName und owner (display und username), wo ich besitzer oder mitglied bin)
  getAllGroups(): Observable<Group[]> {
    return this.httpClient.get<Group[]>(this.groupBaseUri)
  }

  // returns (alle gruppen (mit groupId, groupName), wo ich besitzer bin)
  getMyGroups(): Observable<Group[]> {
    return this.httpClient.get<Group[]>(this.groupBaseUri + "/my")
  }

  getCommonGroups(username: string): Observable<Group[]> {
    return this.httpClient.get<Group[]>(this.groupBaseUri + "/my/" + username)
  }

  // returns (alle rootBoards von Gruppen in denen man drinnen ist mit permission + groupId)
  getGroupRoots(): Observable<Comp[]> {
    return this.httpClient.get<Comp[]>(this.groupBoardUri)
  }

  getBoardGroups(boardId: number): Observable<GroupWithPermission[]> {
    return this.httpClient.get<GroupWithPermission[]>(this.groupBoardUri + "/" + boardId);
  }

  getBoardPermission(boardId: number): Observable<boolean> {
    return this.httpClient.get<boolean>(this.groupBoardUri + "/permission/" + boardId);
  }

  // returns nix glaub ich
  deleteGroup(groupId: number) {
    return this.httpClient.delete(this.groupBaseUri + "/" + groupId)
  }

  // returns nix glaub ich
  leaveGroup(groupId: number) {
    return this.httpClient.delete(this.groupMemberUri + "/leave/" + groupId)
  }
}
