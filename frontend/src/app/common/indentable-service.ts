import { IndentableTreeElem } from "../common/dto/indentable-tree-elem";
import { ParentWithId } from "../common/dto/parent-with-id";

export abstract class IndentableService<T extends ParentWithId> {
    public list: IndentableTreeElem<T>[] = [];

    constructor() { }

    protected sort() {
        this.list.sort((a, b) => a.order - b.order);
        this.calculateRealOrder();
        this.list.sort((a, b) => a.realOrder - b.realOrder);
    }

    private calculateRealOrder() {
        let proj = this.list.filter((a) => a.indent == 0);
        var offset = 0;
        for(let project of proj) {
            project.parentList = [];
            offset += this.countAllChildren(project, offset) +1;
        }
    }
    
    private countAllChildren(project: IndentableTreeElem<T>, offset: number, parent?: IndentableTreeElem<T>): number {
        project.realOrder = offset;
        offset += 1;
        
        if(parent) {
            project.parentList = [...parent.parentList];
            project.parentList.push(parent);
        }

        if(!project.hasChildren) {
            return 0;
        }

        let children = this.list.filter((a) => a.parent?.id == project.id);
        var num = 0;
        for(let proj of children) {
            let childNum = this.countAllChildren(proj, offset, project);
            num += childNum+1;
            offset += childNum+1;      
        } 
        return num;
    }

    protected recalculateChildrenIndent(parentId: number, indent: number) {
        let children = this.list
        .filter((a) => a.parent && a.parent.id == parentId);
        for(let child of children) {
            child.indent = indent;
            this.recalculateChildrenIndent(child.id, indent+1);
        }
    }
    
    protected recalculateHasChildren(project: IndentableTreeElem<T>) {
        let children = this.list.filter((a) => a.parent && a.parent.id == project.id);
        project.hasChildren = children.length > 0 ? true : false;
        for(let parent of project.parentList) {
            let parentChildren = this.list.filter((a) => a.parent && a.parent.id == parent.id);
            parent.hasChildren = parentChildren.length > 0 ? true : false;
        }
    }
}