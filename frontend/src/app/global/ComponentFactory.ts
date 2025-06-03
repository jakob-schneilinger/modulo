import { BaseComponent } from "../components/comp/base/base.component";
import { Component, ComponentType } from "../dtos/component";

class ComponentFactory {
  private constructors: Partial<{ [key in ComponentType]: BaseComponent<Component<key>> }> = {};

  addComponentType<T extends ComponentType, C extends typeof BaseComponent<Component<T>>>(type: T, constructor: C) {
    (this.constructors[type] as any) = constructor;
  }

  get<T extends ComponentType>(type: T): BaseComponent<Component<T>> {
    return this.constructors[type];
  }
}

export default new ComponentFactory();
