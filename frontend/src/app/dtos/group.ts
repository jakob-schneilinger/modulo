import {User} from "./user";

export interface Group {
  id?: number;
  name: string;
  owner?: User;
}

export interface GroupDetail {
  id?: number;
  name: string;
  owner?: User;
  members?: User[];
}

export interface Permission {
  read: boolean;
  write: boolean;
}

/*
export interface GroupWithPermission {
  group: Group;
  permission: Permission;
}

 */

export interface GroupWithPermission {
  groupId: number;
  name: string;
  boardId: number;
  permission: Permission;
}
