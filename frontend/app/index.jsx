import React from 'react';
import {render} from 'react-dom';
import { BrowserRouter, Route, Switch } from 'react-router-dom';

import MainPage from './MainPage.jsx';

window.React = require('react');

class App extends React.Component {
    render () {
        return(<Switch>
            <Route exact path="/" render={props=><MainPage/>}/>
        </Switch>);
    }
}

render(<BrowserRouter><App/></BrowserRouter>, document.getElementById('app'));