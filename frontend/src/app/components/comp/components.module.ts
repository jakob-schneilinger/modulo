import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { BaseComponent } from "./base/base.component";
import { ContainerComponent } from "./containers/container.component";
import { ImageComponent } from "./image/image.component";
import { NoteComponent } from "./containers/note/note.component";
import { TextComponent } from "./text/text.component";
import { FormsModule } from "@angular/forms";
import { BoardComponent } from "./containers/board/board.component";
import { ContextMenuComponent } from "./context-menu/context-menu.component";
import { TaskComponent } from "./containers/task/task.component";

@NgModule({
  declarations: [
    BaseComponent, //
    ContainerComponent,
    ImageComponent,
    NoteComponent,
    TextComponent,
    BoardComponent,
    TaskComponent,
    ContextMenuComponent,
  ],
  imports: [
    CommonModule, //
    FormsModule,
  ],
  exports: [
    // needed within containers
    ContainerComponent,
    BoardComponent,
    ImageComponent,
    NoteComponent,
    TextComponent,
    TaskComponent,
    ContextMenuComponent,
  ],
})
export class ComponentsModule {}
